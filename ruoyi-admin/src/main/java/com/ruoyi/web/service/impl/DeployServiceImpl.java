package com.ruoyi.web.service.impl;

import com.jcraft.jsch.*;
import com.ruoyi.common.config.RuoYiConfig;
import com.ruoyi.web.domain.OpsApp;
import com.ruoyi.web.domain.OpsServer;
import com.ruoyi.web.mapper.OpsAppMapper;
import com.ruoyi.web.mapper.OpsServerMapper;
import com.ruoyi.web.service.IDeployService;
import com.ruoyi.web.service.IOpsDeployLogService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.charset.StandardCharsets;

/**
 * 部署服务实现
 * 
 * @author ruoyi
 */
@Service
public class DeployServiceImpl implements IDeployService {
    
    private static final Logger log = LoggerFactory.getLogger(DeployServiceImpl.class);
    
    @Autowired
    private OpsAppMapper opsAppMapper;
    
    @Autowired
    private OpsServerMapper opsServerMapper;
    
    @Autowired
    private IOpsDeployLogService deployLogService;
    
    @Override
    @Async
    public void executeDeploy(Long logId, Long appId, Long serverId, String deployType) {
        Session session = null;
        ChannelSftp sftpChannel = null;
        ChannelExec execChannel = null;
        
        try {
            // 获取应用和服务器信息
            OpsApp app = opsAppMapper.selectOpsAppById(appId);
            OpsServer server = opsServerMapper.selectOpsServerByServerId(serverId);
            
            if (app == null || server == null) {
                deployLogService.finishDeploy(logId, false, "应用或服务器信息不存在");
                return;
            }
            
            deployLogService.appendLog(logId, String.format("[INFO] 连接服务器: %s (%s)\n", 
                server.getServerName(), server.getPublicIp()));
            
            // 建立 SSH 连接
            JSch jsch = new JSch();
            session = jsch.getSession(server.getUsername(), server.getPublicIp(), server.getServerPort());
            session.setPassword(server.getPassword());
            session.setConfig("StrictHostKeyChecking", "no");
            session.connect(30000);
            
            deployLogService.appendLog(logId, "[SUCCESS] SSH 连接成功\n");
            
            // 根据部署类型执行不同操作
            switch (deployType) {
                case "deploy":
                    deployApplication(session, app, server, logId);
                    break;
                case "start":
                    startApplication(session, app, server, logId);
                    break;
                case "stop":
                    stopApplication(session, app, server, logId);
                    break;
                case "restart":
                    stopApplication(session, app, server, logId);
                    Thread.sleep(2000);
                    startApplication(session, app, server, logId);
                    break;
                default:
                    deployLogService.finishDeploy(logId, false, "未知的部署类型: " + deployType);
                    return;
            }
            
            deployLogService.finishDeploy(logId, true, null);
            
        } catch (Exception e) {
            log.error("部署失败", e);
            deployLogService.finishDeploy(logId, false, e.getMessage());
        } finally {
            if (execChannel != null && execChannel.isConnected()) {
                execChannel.disconnect();
            }
            if (sftpChannel != null && sftpChannel.isConnected()) {
                sftpChannel.disconnect();
            }
            if (session != null && session.isConnected()) {
                session.disconnect();
            }
        }
    }
    
    /**
     * 部署应用
     */
    private void deployApplication(Session session, OpsApp app, OpsServer server, Long logId) throws Exception {
        deployLogService.appendLog(logId, "\n========== 步骤 1/3: 开始部署应用 ==========\n");
        
        // 1. 上传应用包（如果有）
        if (app.getPackagePath() != null && !app.getPackagePath().isEmpty()) {
            deployLogService.appendLog(logId, "\n========== 步骤 2/3: 上传应用包 ==========\n");
            uploadPackage(session, app, server, logId);
        } else {
            deployLogService.appendLog(logId, "\n[SKIP] 跳过应用包上传（未配置应用包）\n");
        }
        
        // 2. 执行启动脚本
        deployLogService.appendLog(logId, "\n========== 步骤 3/3: 执行启动脚本 ==========\n");
        if (app.getStartScript() != null && !app.getStartScript().isEmpty()) {
            executeScript(session, app, app.getStartScript(), logId);
        } else {
            deployLogService.appendLog(logId, "[WARN] 未配置启动脚本\n");
        }
        
        deployLogService.appendLog(logId, "\n========== 部署完成 ==========\n");
    }
    
    /**
     * 启动应用
     */
    private void startApplication(Session session, OpsApp app, OpsServer server, Long logId) throws Exception {
        deployLogService.appendLog(logId, "\n========== 步骤 1/1: 启动应用 ==========\n");
        
        if (app.getStartScript() != null && !app.getStartScript().isEmpty()) {
            executeScript(session, app, app.getStartScript(), logId);
        } else {
            deployLogService.appendLog(logId, "[ERROR] 未配置启动脚本\n");
            throw new Exception("未配置启动脚本");
        }
        
        deployLogService.appendLog(logId, "\n========== 启动完成 ==========\n");
    }
    
    /**
     * 停止应用
     */
    private void stopApplication(Session session, OpsApp app, OpsServer server, Long logId) throws Exception {
        deployLogService.appendLog(logId, "\n========== 步骤 1/1: 停止应用 ==========\n");
        
        if (app.getStopScript() != null && !app.getStopScript().isEmpty()) {
            executeScript(session, null, app.getStopScript(), logId);
        } else {
            deployLogService.appendLog(logId, "[WARN] 未配置停止脚本，尝试使用默认方式停止\n");
            // 默认停止方式：查找并 kill 进程
            String defaultStopScript = String.format(
                "ps aux | grep '%s' | grep -v grep | awk '{print $2}' | xargs kill -15",
                app.getAppName()
            );
            executeScript(session, null, defaultStopScript, logId);
        }
        
        deployLogService.appendLog(logId, "\n========== 停止完成 ==========\n");
    }
    
    /**
     * 上传应用包
     */
    private void uploadPackage(Session session, OpsApp app, OpsServer server, Long logId) throws Exception {
        deployLogService.appendLog(logId, "[INFO] 准备上传应用包...\n");
        
        ChannelSftp sftpChannel = (ChannelSftp) session.openChannel("sftp");
        sftpChannel.connect();
        
        try {
            // 本地文件路径
            String packagePath = app.getPackagePath();
            String localPath;
            
            if (packagePath.startsWith("/profile/")) {
                localPath = RuoYiConfig.getProfile() + packagePath.substring(8);
            } else if (packagePath.startsWith("profile/")) {
                localPath = RuoYiConfig.getProfile() + "/" + packagePath.substring(8);
            } else {
                localPath = packagePath;
            }
            
            File localFile = new File(localPath);
            
            if (!localFile.exists()) {
                deployLogService.appendLog(logId, String.format("[ERROR] 本地文件不存在: %s\n", localPath));
                throw new Exception("本地文件不存在: " + localPath);
            }
            
            deployLogService.appendLog(logId, String.format("[INFO] 本地文件: %s (%.2f MB)\n", 
                localFile.getName(), localFile.length() / 1024.0 / 1024.0));
            
            // 远程目标路径
            String remotePath = app.getDeployPath();
            String remoteFile = remotePath + "/" + localFile.getName();
            
            // 创建远程目录
            deployLogService.appendLog(logId, String.format("[INFO] 创建远程目录: %s\n", remotePath));
            createRemoteDir(sftpChannel, remotePath);
            
            // 上传文件
            deployLogService.appendLog(logId, String.format("[INFO] 开始上传: %s -> %s\n", 
                localFile.getName(), remoteFile));
            
            sftpChannel.put(new FileInputStream(localFile), remoteFile, ChannelSftp.OVERWRITE);
            
            deployLogService.appendLog(logId, "[SUCCESS] 文件上传成功\n");
            
        } finally {
            if (sftpChannel != null && sftpChannel.isConnected()) {
                sftpChannel.disconnect();
            }
        }
    }
    
    /**
     * 创建远程目录
     */
    private void createRemoteDir(ChannelSftp sftpChannel, String path) throws SftpException {
        String[] dirs = path.split("/");
        String currentPath = "";
        
        for (String dir : dirs) {
            if (dir.isEmpty()) continue;
            
            currentPath += "/" + dir;
            try {
                sftpChannel.cd(currentPath);
            } catch (SftpException e) {
                sftpChannel.mkdir(currentPath);
                sftpChannel.cd(currentPath);
            }
        }
    }
    
    /**
     * 构建增强脚本：自动处理 Java 环境变量问题和配置文件路径
     * 在用户脚本前添加环境检查、Java 路径查找和配置文件参数
     */
    private String buildEnhancedScript(OpsApp app, String userScript) {
        StringBuilder sb = new StringBuilder();
        
        // 脚本头部
        sb.append("#!/bin/bash\n");
        sb.append("# Auto-generated enhanced deployment script\n");
        sb.append("# Handles Java environment for non-interactive SSH sessions\n\n");
        
        // 加载环境变量
        sb.append("# ===== 加载环境变量 =====\n");
        sb.append("if [ -f /etc/profile ]; then\n");
        sb.append("    source /etc/profile 2>/dev/null || true\n");
        sb.append("fi\n\n");
        
        sb.append("if [ -f ~/.bash_profile ]; then\n");
        sb.append("    source ~/.bash_profile 2>/dev/null || true\n");
        sb.append("elif [ -f ~/.bashrc ]; then\n");
        sb.append("    source ~/.bashrc 2>/dev/null || true\n");
        sb.append("fi\n\n");
        
        // 查找并设置 Java 路径
        sb.append("# ===== 查找 Java 路径 =====\n");
        sb.append("JAVA_CMD=\"\"\n\n");
        
        sb.append("# 方法1: 使用 which 命令\n");
        sb.append("if command -v java &> /dev/null; then\n");
        sb.append("    JAVA_CMD=$(which java 2>/dev/null)\n");
        sb.append("    echo \"[INFO] 找到 Java (which): ${JAVA_CMD}\"\n");
        sb.append("fi\n\n");
        
        sb.append("# 方法2: 如果 which 失败，尝试常见路径\n");
        sb.append("if [ -z \"${JAVA_CMD}\" ] || [ ! -f \"${JAVA_CMD}\" ]; then\n");
        sb.append("    echo \"[WARN] which 命令未找到 java，尝试常见路径...\"\n");
        sb.append("    for path in /usr/bin/java /usr/local/bin/java /usr/local/java/bin/java /usr/java/latest/bin/java /opt/java/bin/java; do\n");
        sb.append("        if [ -f \"${path}\" ]; then\n");
        sb.append("            JAVA_CMD=\"${path}\"\n");
        sb.append("            echo \"[INFO] 找到 Java (常见路径): ${JAVA_CMD}\"\n");
        sb.append("            break\n");
        sb.append("        fi\n");
        sb.append("    done\n");
        sb.append("fi\n\n");
        
        sb.append("# 验证 Java 是否可用\n");
        sb.append("if [ -z \"${JAVA_CMD}\" ] || [ ! -f \"${JAVA_CMD}\" ]; then\n");
        sb.append("    echo \"[ERROR] 无法找到 Java，请检查 Java 安装\"\n");
        sb.append("    echo \"[ERROR] 当前 PATH: ${PATH}\"\n");
        sb.append("    exit 1\n");
        sb.append("fi\n\n");
        
        sb.append("echo \"[SUCCESS] Java 环境检查通过\"\n");
        sb.append("echo \"[INFO] Java 路径: ${JAVA_CMD}\"\n");
        sb.append("${JAVA_CMD} -version 2>&1 | head -3\n");
        sb.append("echo \"\"\n\n");
        
        // 处理配置文件路径
        if (app != null && app.getConfigFilePath() != null && !app.getConfigFilePath().trim().isEmpty()) {
            String configPath = app.getConfigFilePath().trim();
            sb.append("# ===== 配置文件路径 =====\n");
            sb.append("CONFIG_FILE_PATH=\"").append(configPath).append("\"\n");
            sb.append("echo \"[INFO] 使用外部配置路径: ${CONFIG_FILE_PATH}\"\n\n");
            
            // 验证配置路径是否存在（可以是文件或目录）
            sb.append("# 验证配置路径\n");
            sb.append("if [ ! -e \"${CONFIG_FILE_PATH}\" ]; then\n");
            sb.append("    echo \"[ERROR] 配置路径不存在: ${CONFIG_FILE_PATH}\"\n");
            sb.append("    exit 1\n");
            sb.append("fi\n\n");
            
            // 判断是文件还是目录
            sb.append("if [ -f \"${CONFIG_FILE_PATH}\" ]; then\n");
            sb.append("    echo \"[INFO] 配置类型: 文件\"\n");
            sb.append("elif [ -d \"${CONFIG_FILE_PATH}\" ]; then\n");
            sb.append("    echo \"[INFO] 配置类型: 目录\"\n");
            sb.append("    echo \"[INFO] 目录内容:\"\n");
            sb.append("    ls -la \"${CONFIG_FILE_PATH}\" | grep -E '\\.yml$|\\.yaml$|\\.properties$' | sed 's/^/  /'\n");
            sb.append("fi\n");
            sb.append("echo \"[SUCCESS] 配置路径验证通过\"\n");
            sb.append("echo \"[INFO] 使用 spring.config.additional-location 追加配置（保留 JAR 包内配置）\"\n");
            sb.append("echo \"\"\n\n");
        }
        
        // 替换用户脚本中的 java 命令为 ${JAVA_CMD}
        sb.append("# ===== 用户部署脚本 =====\n");
        String processedScript = userScript
            .replaceAll("(?<!\\$\\{JAVA_CMD\\})\\bjava\\b", "\\${JAVA_CMD}"); // 替换独立的 java 命令
        
        // 从 Spring Boot 参数中提取端口号，动态替换脚本中的 PORT 变量
        if (app != null && app.getSpringParams() != null && !app.getSpringParams().trim().isEmpty()) {
            String springParams = app.getSpringParams().trim();
            // 使用正则提取 --server.port=xxxx 或 -Dserver.port=xxxx
            java.util.regex.Pattern portPattern = java.util.regex.Pattern.compile("(?:--server\\.port=|-Dserver\\.port=)(\\d+)");
            java.util.regex.Matcher matcher = portPattern.matcher(springParams);
            if (matcher.find()) {
                String configuredPort = matcher.group(1);
                // 替换脚本中的 PORT=xxxx 为配置的端口
                processedScript = processedScript.replaceAll("PORT=\\d+", "PORT=" + configuredPort);
                sb.append("echo \"[INFO] 使用配置的端口: ").append(configuredPort).append("\"\n");
            }
        }
        
        // 构建需要追加到 -jar xxx.jar 后的参数
        StringBuilder additionalParams = new StringBuilder();
        
        // 如果配置了配置文件路径，添加 --spring.config.additional-location
        if (app != null && app.getConfigFilePath() != null && !app.getConfigFilePath().trim().isEmpty()) {
            additionalParams.append(" --spring.config.additional-location=file:${CONFIG_FILE_PATH}");
        }
        
        // 如果配置了 Spring Boot 参数，添加这些参数
        if (app != null && app.getSpringParams() != null && !app.getSpringParams().trim().isEmpty()) {
            String springParams = app.getSpringParams().trim();
            additionalParams.append(" ").append(springParams);
        }
        
        // 一次性替换：在 -jar xxx.jar 后添加所有参数
        if (additionalParams.length() > 0) {
            processedScript = processedScript.replaceAll(
                "(-jar\\s+[^\\s]+\\.jar)",
                "\\$1" + additionalParams.toString()
            );
        }
        
        sb.append(processedScript);
        
        return sb.toString();
    }
    
    /**
     * 执行脚本
     */
    private void executeScript(Session session, OpsApp app, String script, Long logId) throws Exception {
        ChannelSftp sftpChannel = null;
        ChannelExec execChannel = null;
        
        try {
            // 方案：将脚本上传到服务器临时文件，然后执行
            String tempScriptPath = "/tmp/deploy_script_" + System.currentTimeMillis() + ".sh";
            
            deployLogService.appendLog(logId, String.format("[INFO] 上传脚本到服务器: %s\n", tempScriptPath));
            
            // 增强脚本：添加环境变量加载、Java 路径查找和配置文件参数
            String enhancedScript = buildEnhancedScript(app, script);
            
            // 上传脚本文件
            sftpChannel = (ChannelSftp) session.openChannel("sftp");
            sftpChannel.connect();
            
            ByteArrayInputStream scriptStream = new ByteArrayInputStream(enhancedScript.getBytes(StandardCharsets.UTF_8));
            sftpChannel.put(scriptStream, tempScriptPath);
            sftpChannel.disconnect();
            sftpChannel = null;
            
            deployLogService.appendLog(logId, "[SUCCESS] 脚本上传成功\n");
            
            // 赋予执行权限
            deployLogService.appendLog(logId, "[INFO] 设置脚本执行权限\n");
            execChannel = (ChannelExec) session.openChannel("exec");
            execChannel.setCommand("chmod +x " + tempScriptPath);
            execChannel.connect();
            
            // 等待 chmod 完成
            while (!execChannel.isClosed()) {
                Thread.sleep(100);
            }
            execChannel.disconnect();
            execChannel = null;
            
            // 执行脚本
            deployLogService.appendLog(logId, "[INFO] 开始执行脚本\n");
            execChannel = (ChannelExec) session.openChannel("exec");
            
            // 重要：设置 PTY 以确保输出不会被缓冲
            execChannel.setPty(true);
            
            // 使用 bash -x 执行脚本以获得详细的调试输出
            execChannel.setCommand("bash -x " + tempScriptPath + " 2>&1");
            
            InputStream in = execChannel.getInputStream();
            
            execChannel.connect();
            deployLogService.appendLog(logId, "[INFO] SSH 通道已连接，开始读取输出\n");
            
            // 使用非阻塞方式读取输出，增加超时机制
            byte[] tmp = new byte[4096];
            int idleCount = 0;
            int maxIdleCount = 600; // 60秒超时 (100ms * 600)
            
            while (true) {
                boolean hasData = false;
                
                // 读取输出
                while (in.available() > 0) {
                    int i = in.read(tmp, 0, 4096);
                    if (i < 0) break;
                    String output = new String(tmp, 0, i, StandardCharsets.UTF_8);
                    deployLogService.appendLog(logId, output);
                    hasData = true;
                    idleCount = 0; // 重置空闲计数
                }
                
                // 检查是否执行完成
                if (execChannel.isClosed()) {
                    deployLogService.appendLog(logId, "[INFO] 脚本执行通道已关闭\n");
                    // 最后再读取一次，确保所有输出都被读取
                    if (in.available() > 0) {
                        deployLogService.appendLog(logId, "[INFO] 读取剩余输出...\n");
                        continue;
                    }
                    break;
                }
                
                // 如果没有数据，增加空闲计数
                if (!hasData) {
                    idleCount++;
                    if (idleCount >= maxIdleCount) {
                        deployLogService.appendLog(logId, "[WARN] 脚本执行超时（60秒无输出），强制结束\n");
                        break;
                    }
                }
                
                Thread.sleep(100);
            }
            
            int exitStatus = execChannel.getExitStatus();
            deployLogService.appendLog(logId, String.format("[INFO] 脚本执行完成，退出码: %d\n", exitStatus));
            
            // 清理临时脚本
            execChannel.disconnect();
            execChannel = null;
            
            deployLogService.appendLog(logId, "[INFO] 清理临时脚本文件\n");
            ChannelExec cleanupChannel = (ChannelExec) session.openChannel("exec");
            cleanupChannel.setCommand("rm -f " + tempScriptPath);
            cleanupChannel.connect();
            while (!cleanupChannel.isClosed()) {
                Thread.sleep(100);
            }
            cleanupChannel.disconnect();
            
            if (exitStatus != 0) {
                deployLogService.appendLog(logId, String.format("[ERROR] 脚本执行失败，退出码: %d\n", exitStatus));
                throw new Exception("脚本执行失败，退出码: " + exitStatus);
            }
            
            deployLogService.appendLog(logId, "[SUCCESS] 脚本执行成功\n");
            
        } finally {
            if (sftpChannel != null && sftpChannel.isConnected()) {
                sftpChannel.disconnect();
            }
            if (execChannel != null && execChannel.isConnected()) {
                execChannel.disconnect();
            }
        }
    }
}
