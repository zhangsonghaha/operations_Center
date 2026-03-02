package com.ruoyi.web.service.impl;

import com.alibaba.fastjson2.JSON;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.web.domain.DockerContainer;
import com.ruoyi.web.domain.OpsServer;
import com.ruoyi.web.domain.dto.DockerDeployConfig;
import com.ruoyi.web.domain.dto.DockerDeployResult;
import com.ruoyi.web.domain.dto.DockerEnvInfo;
import com.ruoyi.web.mapper.DockerContainerMapper;
import com.ruoyi.web.mapper.OpsServerMapper;
import com.ruoyi.web.service.IDockerDeployService;
import com.ruoyi.web.service.IDockerEnvService;
import com.ruoyi.web.service.IDockerImageService;
import com.ruoyi.web.service.IDockerValidationService;
import com.ruoyi.web.service.IOpsDeployLogService;
import com.ruoyi.web.utils.DockerCommandBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * Docker部署服务实现
 * 
 * @author ruoyi
 */
@Service
public class DockerDeployServiceImpl implements IDockerDeployService
{
    private static final Logger log = LoggerFactory.getLogger(DockerDeployServiceImpl.class);
    
    /** SSH连接超时时间（毫秒） */
    private static final int SSH_CONNECT_TIMEOUT = 30000;
    
    /** 命令执行超时时间（毫秒） */
    private static final int COMMAND_TIMEOUT = 5000;
    
    /** 容器ID正则表达式（64位十六进制） */
    private static final String CONTAINER_ID_PATTERN = "^[a-f0-9]{64}$";
    
    @Autowired
    private DockerContainerMapper dockerContainerMapper;
    
    @Autowired
    private OpsServerMapper opsServerMapper;
    
    @Autowired
    private IDockerEnvService dockerEnvService;
    
    @Autowired
    private IDockerValidationService dockerValidationService;
    
    @Autowired
    private IDockerImageService dockerImageService;
    
    @Autowired
    private IOpsDeployLogService deployLogService;
    
    /**
     * 部署Docker容器
     */
    @Override
    @Async
    public DockerDeployResult deployContainer(DockerDeployConfig config) throws Exception
    {
        // 参数验证
        if (config == null)
        {
            throw new IllegalArgumentException("部署配置不能为空");
        }
        if (config.getServerId() == null)
        {
            throw new IllegalArgumentException("服务器ID不能为空");
        }
        
        DockerDeployResult result = new DockerDeployResult();
        result.setSuccess(false);
        
        // 创建部署日志
        Long logId = deployLogService.startDeploy(null, config.getServerId(), "docker");
        result.setDeployLogId(logId);
        
        Session session = null;
        String createdContainerId = null;
        
        try
        {
            deployLogService.appendLog(logId, "========== 开始 Docker 容器部署 ==========\n");
            deployLogService.appendLog(logId, String.format("[INFO] 容器名称: %s\n", config.getContainerName()));
            deployLogService.appendLog(logId, String.format("[INFO] 镜像: %s:%s\n", 
                config.getImageName(), 
                StringUtils.isEmpty(config.getImageTag()) ? "latest" : config.getImageTag()));
            
            // 获取服务器信息
            OpsServer server = opsServerMapper.selectOpsServerByServerId(config.getServerId());
            if (server == null)
            {
                throw new Exception("服务器不存在");
            }
            
            deployLogService.appendLog(logId, String.format("[INFO] 目标服务器: %s (%s)\n", 
                server.getServerName(), server.getPublicIp()));
            
            // 建立SSH连接
            deployLogService.appendLog(logId, "\n========== 步骤 1/6: 建立SSH连接 ==========\n");
            session = createSSHSession(server, logId);
            deployLogService.appendLog(logId, "[SUCCESS] SSH连接成功\n");
            
            // 步骤1: 环境检测
            deployLogService.appendLog(logId, "\n========== 步骤 2/6: Docker环境检测 ==========\n");
            checkDockerEnvironment(session, config.getServerId(), logId);
            
            // 步骤2: 部署前验证
            deployLogService.appendLog(logId, "\n========== 步骤 3/6: 部署前验证 ==========\n");
            performPreDeployValidation(session, config, logId);
            
            // 步骤3: 镜像拉取
            deployLogService.appendLog(logId, "\n========== 步骤 4/6: 镜像拉取 ==========\n");
            pullDockerImage(session, config, logId);
            
            // 步骤4: 容器创建和启动
            deployLogService.appendLog(logId, "\n========== 步骤 5/6: 容器创建和启动 ==========\n");
            createdContainerId = createAndStartContainer(session, config, logId);
            result.setContainerId(createdContainerId);
            
            // 步骤5: 保存容器记录
            deployLogService.appendLog(logId, "\n========== 步骤 6/6: 保存容器记录 ==========\n");
            Long containerDbId = saveContainerRecord(config, createdContainerId, logId);
            result.setContainerRecordId(containerDbId);
            
            // 部署成功
            deployLogService.appendLog(logId, "\n========== 部署完成 ==========\n");
            deployLogService.appendLog(logId, String.format("[SUCCESS] 容器ID: %s\n", createdContainerId.substring(0, 12)));
            deployLogService.appendLog(logId, String.format("[SUCCESS] 容器名称: %s\n", config.getContainerName()));
            deployLogService.finishDeploy(logId, true, null);
            
            result.setSuccess(true);
            result.setMessage("部署成功");
            
        }
        catch (Exception e)
        {
            log.error("Docker容器部署失败", e);
            
            // 记录失败日志
            deployLogService.appendLog(logId, String.format("\n[ERROR] 部署失败: %s\n", e.getMessage()));
            
            // 如果容器已创建，尝试回滚（删除容器）
            if (createdContainerId != null && session != null)
            {
                try
                {
                    deployLogService.appendLog(logId, "\n========== 开始回滚 ==========\n");
                    deployLogService.appendLog(logId, "[INFO] 尝试删除已创建的容器...\n");
                    
                    String rmCommand = DockerCommandBuilder.buildDockerRmCommand(config.getContainerName(), true);
                    executeCommand(session, rmCommand, logId);
                    
                    deployLogService.appendLog(logId, "[SUCCESS] 容器已删除\n");
                }
                catch (Exception rollbackEx)
                {
                    log.error("回滚失败", rollbackEx);
                    deployLogService.appendLog(logId, String.format("[WARN] 回滚失败: %s\n", rollbackEx.getMessage()));
                }
            }
            
            deployLogService.finishDeploy(logId, false, e.getMessage());
            
            result.setSuccess(false);
            result.setMessage("部署失败: " + e.getMessage());
            result.setErrorMessage(e.getMessage());
        }
        finally
        {
            // 关闭SSH连接
            if (session != null && session.isConnected())
            {
                session.disconnect();
                log.info("SSH连接已关闭");
            }
        }
        
        return result;
    }

    /**
     * 创建SSH会话
     */
    private Session createSSHSession(OpsServer server, Long logId) throws Exception
    {
        deployLogService.appendLog(logId, String.format("[INFO] 连接服务器: %s:%d\n", 
            server.getPublicIp(), server.getServerPort()));
        
        JSch jsch = new JSch();
        Session session = jsch.getSession(server.getUsername(), server.getPublicIp(), server.getServerPort());
        session.setPassword(server.getPassword());
        session.setConfig("StrictHostKeyChecking", "no");
        session.connect(SSH_CONNECT_TIMEOUT);
        
        return session;
    }
    
    /**
     * 检查Docker环境
     */
    private void checkDockerEnvironment(Session session, Long serverId, Long logId) throws Exception
    {
        deployLogService.appendLog(logId, "[INFO] 开始检测Docker环境...\n");
        
        DockerEnvInfo envInfo = dockerEnvService.checkDockerEnvironment(session);
        
        // 检查Docker是否安装
        if (!envInfo.isDockerInstalled())
        {
            throw new Exception("Docker未安装，请先安装Docker");
        }
        deployLogService.appendLog(logId, String.format("[SUCCESS] Docker已安装: %s\n", envInfo.getDockerVersion()));
        
        // 检查Docker服务是否运行
        if (!envInfo.isDockerRunning())
        {
            throw new Exception("Docker服务未运行，请启动Docker服务");
        }
        deployLogService.appendLog(logId, "[SUCCESS] Docker服务正在运行\n");
        
        // 显示存储信息
        if (StringUtils.isNotEmpty(envInfo.getStorageDriver()))
        {
            deployLogService.appendLog(logId, String.format("[INFO] 存储驱动: %s\n", envInfo.getStorageDriver()));
        }
        if (StringUtils.isNotEmpty(envInfo.getDockerRootDir()))
        {
            deployLogService.appendLog(logId, String.format("[INFO] Docker根目录: %s\n", envInfo.getDockerRootDir()));
        }
        if (envInfo.getTotalSpace() != null && envInfo.getTotalSpace() > 0)
        {
            double totalGB = envInfo.getTotalSpace() / 1024.0 / 1024.0 / 1024.0;
            double usedGB = envInfo.getUsedSpace() / 1024.0 / 1024.0 / 1024.0;
            double freeGB = totalGB - usedGB;
            deployLogService.appendLog(logId, String.format("[INFO] 磁盘空间: %.2f GB / %.2f GB (剩余: %.2f GB)\n", 
                usedGB, totalGB, freeGB));
        }
        
        deployLogService.appendLog(logId, "[SUCCESS] Docker环境检测通过\n");
    }
    
    /**
     * 执行部署前验证
     */
    private void performPreDeployValidation(Session session, DockerDeployConfig config, Long logId) throws Exception
    {
        // 1. 验证容器名称格式
        deployLogService.appendLog(logId, "[INFO] 验证容器名称格式...\n");
        dockerValidationService.validateContainerName(config.getContainerName());
        deployLogService.appendLog(logId, "[SUCCESS] 容器名称格式正确\n");
        
        // 2. 检查容器名称是否已存在
        deployLogService.appendLog(logId, "[INFO] 检查容器名称冲突...\n");
        boolean nameExists = dockerValidationService.checkContainerNameExists(session, config.getContainerName());
        if (nameExists)
        {
            throw new Exception("容器名称已存在: " + config.getContainerName());
        }
        deployLogService.appendLog(logId, "[SUCCESS] 容器名称可用\n");
        
        // 3. 检查端口冲突
        if (config.getPorts() != null && !config.getPorts().isEmpty())
        {
            deployLogService.appendLog(logId, "[INFO] 检查端口冲突...\n");
            for (com.ruoyi.web.domain.dto.PortMapping port : config.getPorts())
            {
                boolean portInUse = dockerValidationService.checkPortInUse(session, port.getHostPort());
                if (portInUse)
                {
                    throw new Exception("端口已被占用: " + port.getHostPort());
                }
                deployLogService.appendLog(logId, String.format("[SUCCESS] 端口 %d 可用\n", port.getHostPort()));
            }
        }
        
        // 4. 验证卷挂载安全性
        if (config.getVolumes() != null && !config.getVolumes().isEmpty())
        {
            deployLogService.appendLog(logId, "[INFO] 验证卷挂载安全性...\n");
            dockerValidationService.validateVolumeMounts(config.getVolumes());
            deployLogService.appendLog(logId, "[SUCCESS] 卷挂载验证通过\n");
        }
        
        deployLogService.appendLog(logId, "[SUCCESS] 部署前验证通过\n");
    }
    
    /**
     * 拉取Docker镜像
     */
    private void pullDockerImage(Session session, DockerDeployConfig config, Long logId) throws Exception
    {
        String imageTag = StringUtils.isEmpty(config.getImageTag()) ? "latest" : config.getImageTag();
        
        deployLogService.appendLog(logId, String.format("[INFO] 开始拉取镜像: %s:%s\n", 
            config.getImageName(), imageTag));
        
        boolean success = dockerImageService.pullImageWithProgress(
            session, 
            config.getImageName(), 
            imageTag, 
            config.getServerId(), 
            logId
        );
        
        if (!success)
        {
            throw new Exception("镜像拉取失败");
        }
    }
    
    /**
     * 创建并启动容器
     */
    private String createAndStartContainer(Session session, DockerDeployConfig config, Long logId) throws Exception
    {
        // 1. 构建docker run命令
        deployLogService.appendLog(logId, "[INFO] 构建容器启动命令...\n");
        String dockerRunCommand = DockerCommandBuilder.buildDockerRunCommand(config);
        deployLogService.appendLog(logId, String.format("[INFO] 执行命令: %s\n", dockerRunCommand));
        
        // 2. 执行命令
        String output = executeCommand(session, dockerRunCommand, logId);
        
        // 3. 提取容器ID（docker run返回完整的64位容器ID）
        String containerId = output.trim();
        
        // 4. 验证容器ID格式
        if (!containerId.matches(CONTAINER_ID_PATTERN))
        {
            throw new Exception("容器创建失败，返回的容器ID格式不正确: " + containerId);
        }
        
        deployLogService.appendLog(logId, String.format("[SUCCESS] 容器创建成功: %s\n", containerId.substring(0, 12)));
        
        // 5. 验证容器是否正在运行
        deployLogService.appendLog(logId, "[INFO] 验证容器状态...\n");
        boolean isRunning = checkContainerRunning(session, config.getContainerName(), logId);
        
        if (!isRunning)
        {
            throw new Exception("容器创建成功但未能启动");
        }
        
        deployLogService.appendLog(logId, "[SUCCESS] 容器正在运行\n");
        
        return containerId;
    }
    
    /**
     * 检查容器是否正在运行
     */
    private boolean checkContainerRunning(Session session, String containerName, Long logId) throws Exception
    {
        String command = "docker ps --filter name=" + containerName + " --format '{{.Names}}'";
        String output = executeCommand(session, command, logId);
        
        return output.trim().equals(containerName);
    }
    
    /**
     * 保存容器记录到数据库
     */
    private Long saveContainerRecord(DockerDeployConfig config, String containerId, Long logId) throws Exception
    {
        deployLogService.appendLog(logId, "[INFO] 保存容器记录到数据库...\n");
        
        DockerContainer container = new DockerContainer();
        container.setContainerName(config.getContainerName());
        container.setContainerDockerId(containerId);  // 使用 Docker 容器ID（String类型）
        container.setImageName(config.getImageName());
        container.setImageTag(StringUtils.isEmpty(config.getImageTag()) ? "latest" : config.getImageTag());
        container.setServerId(config.getServerId());
        container.setContainerStatus("running");
        
        // 序列化配置为JSON
        if (config.getPorts() != null && !config.getPorts().isEmpty())
        {
            container.setPortMappings(JSON.toJSONString(config.getPorts()));
        }
        if (config.getEnvVars() != null && !config.getEnvVars().isEmpty())
        {
            container.setEnvVars(JSON.toJSONString(config.getEnvVars()));
        }
        if (config.getVolumes() != null && !config.getVolumes().isEmpty())
        {
            container.setVolumeMounts(JSON.toJSONString(config.getVolumes()));
        }
        if (config.getCpuLimit() != null)
        {
            container.setCpuLimit(config.getCpuLimit());
        }
        if (StringUtils.isNotEmpty(config.getMemoryLimit()))
        {
            container.setMemoryLimit(config.getMemoryLimit());
        }
        if (StringUtils.isNotEmpty(config.getRestartPolicy()))
        {
            container.setRestartPolicy(config.getRestartPolicy());
        }
        if (StringUtils.isNotEmpty(config.getNetworkMode()))
        {
            container.setNetworkMode(config.getNetworkMode());
        }
        
        container.setDeployLogId(logId);
        container.setCreateTime(new Date());
        
        dockerContainerMapper.insertDockerContainer(container);
        
        deployLogService.appendLog(logId, String.format("[SUCCESS] 容器记录已保存 (ID: %d)\n", container.getContainerId()));
        
        return container.getContainerId();
    }
    
    /**
     * 执行SSH命令
     */
    private String executeCommand(Session session, String command, Long logId) throws Exception
    {
        ChannelExec channel = null;
        try
        {
            channel = (ChannelExec) session.openChannel("exec");
            channel.setCommand(command);
            
            InputStream in = channel.getInputStream();
            InputStream err = channel.getErrStream();
            
            channel.connect(COMMAND_TIMEOUT);
            
            // 读取输出
            BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
            BufferedReader errReader = new BufferedReader(new InputStreamReader(err, StandardCharsets.UTF_8));
            
            StringBuilder output = new StringBuilder();
            StringBuilder errorOutput = new StringBuilder();
            String line;
            
            // 等待命令执行完成
            int timeout = 0;
            while (!channel.isClosed() && timeout < COMMAND_TIMEOUT)
            {
                // 读取标准输出
                while (reader.ready())
                {
                    line = reader.readLine();
                    if (line != null)
                    {
                        output.append(line).append("\n");
                    }
                }
                
                // 读取错误输出
                while (errReader.ready())
                {
                    line = errReader.readLine();
                    if (line != null)
                    {
                        errorOutput.append(line).append("\n");
                    }
                }
                
                Thread.sleep(100);
                timeout += 100;
            }
            
            // 最后再读取一次
            while (reader.ready())
            {
                line = reader.readLine();
                if (line != null)
                {
                    output.append(line).append("\n");
                }
            }
            while (errReader.ready())
            {
                line = errReader.readLine();
                if (line != null)
                {
                    errorOutput.append(line).append("\n");
                }
            }
            
            int exitStatus = channel.getExitStatus();
            
            // 如果有错误输出或退出码非0，抛出异常
            if (exitStatus != 0 || errorOutput.length() > 0)
            {
                String errorMsg = errorOutput.length() > 0 ? errorOutput.toString() : "命令执行失败";
                throw new Exception(errorMsg);
            }
            
            return output.toString();
        }
        finally
        {
            if (channel != null && channel.isConnected())
            {
                channel.disconnect();
            }
        }
    }

    /**
     * 停止Docker容器
     */
    @Override
    public boolean stopContainer(Long containerId, Long serverId) throws Exception
    {
        // 查询容器信息
        DockerContainer container = dockerContainerMapper.selectDockerContainerById(containerId);
        if (container == null)
        {
            throw new Exception("容器不存在");
        }
        
        // 获取服务器信息
        OpsServer server = opsServerMapper.selectOpsServerByServerId(serverId);
        if (server == null)
        {
            throw new Exception("服务器不存在");
        }
        
        Session session = null;
        try
        {
            // 建立SSH连接
            JSch jsch = new JSch();
            session = jsch.getSession(server.getUsername(), server.getPublicIp(), server.getServerPort());
            session.setPassword(server.getPassword());
            session.setConfig("StrictHostKeyChecking", "no");
            session.connect(SSH_CONNECT_TIMEOUT);
            
            // 执行停止命令
            String command = DockerCommandBuilder.buildDockerStopCommand(container.getContainerName());
            executeCommandSimple(session, command);
            
            // 更新数据库状态
            container.setContainerStatus("stopped");
            dockerContainerMapper.updateDockerContainer(container);
            
            log.info("容器已停止: {}", container.getContainerName());
            return true;
        }
        finally
        {
            if (session != null && session.isConnected())
            {
                session.disconnect();
            }
        }
    }
    
    /**
     * 启动Docker容器
     */
    @Override
    public boolean startContainer(Long containerId, Long serverId) throws Exception
    {
        // 查询容器信息
        DockerContainer container = dockerContainerMapper.selectDockerContainerById(containerId);
        if (container == null)
        {
            throw new Exception("容器不存在");
        }
        
        // 获取服务器信息
        OpsServer server = opsServerMapper.selectOpsServerByServerId(serverId);
        if (server == null)
        {
            throw new Exception("服务器不存在");
        }
        
        Session session = null;
        try
        {
            // 建立SSH连接
            JSch jsch = new JSch();
            session = jsch.getSession(server.getUsername(), server.getPublicIp(), server.getServerPort());
            session.setPassword(server.getPassword());
            session.setConfig("StrictHostKeyChecking", "no");
            session.connect(SSH_CONNECT_TIMEOUT);
            
            // 执行启动命令
            String command = DockerCommandBuilder.buildDockerStartCommand(container.getContainerName());
            executeCommandSimple(session, command);
            
            // 更新数据库状态
            container.setContainerStatus("running");
            dockerContainerMapper.updateDockerContainer(container);
            
            log.info("容器已启动: {}", container.getContainerName());
            return true;
        }
        finally
        {
            if (session != null && session.isConnected())
            {
                session.disconnect();
            }
        }
    }
    
    /**
     * 重启Docker容器
     */
    @Override
    public boolean restartContainer(Long containerId, Long serverId) throws Exception
    {
        // 查询容器信息
        DockerContainer container = dockerContainerMapper.selectDockerContainerById(containerId);
        if (container == null)
        {
            throw new Exception("容器不存在");
        }
        
        // 获取服务器信息
        OpsServer server = opsServerMapper.selectOpsServerByServerId(serverId);
        if (server == null)
        {
            throw new Exception("服务器不存在");
        }
        
        Session session = null;
        try
        {
            // 建立SSH连接
            JSch jsch = new JSch();
            session = jsch.getSession(server.getUsername(), server.getPublicIp(), server.getServerPort());
            session.setPassword(server.getPassword());
            session.setConfig("StrictHostKeyChecking", "no");
            session.connect(SSH_CONNECT_TIMEOUT);
            
            // 执行重启命令
            String command = DockerCommandBuilder.buildDockerRestartCommand(container.getContainerName());
            executeCommandSimple(session, command);
            
            // 更新数据库状态
            container.setContainerStatus("running");
            dockerContainerMapper.updateDockerContainer(container);
            
            log.info("容器已重启: {}", container.getContainerName());
            return true;
        }
        finally
        {
            if (session != null && session.isConnected())
            {
                session.disconnect();
            }
        }
    }
    
    /**
     * 删除Docker容器
     */
    @Override
    public boolean removeContainer(Long containerId, Long serverId, boolean force) throws Exception
    {
        // 查询容器信息
        DockerContainer container = dockerContainerMapper.selectDockerContainerById(containerId);
        if (container == null)
        {
            throw new Exception("容器不存在");
        }
        
        // 获取服务器信息
        OpsServer server = opsServerMapper.selectOpsServerByServerId(serverId);
        if (server == null)
        {
            throw new Exception("服务器不存在");
        }
        
        Session session = null;
        try
        {
            // 建立SSH连接
            JSch jsch = new JSch();
            session = jsch.getSession(server.getUsername(), server.getPublicIp(), server.getServerPort());
            session.setPassword(server.getPassword());
            session.setConfig("StrictHostKeyChecking", "no");
            session.connect(SSH_CONNECT_TIMEOUT);
            
            // 执行删除命令
            String command = DockerCommandBuilder.buildDockerRmCommand(container.getContainerName(), force);
            executeCommandSimple(session, command);
            
            // 从数据库删除记录
            dockerContainerMapper.deleteDockerContainerById(containerId);
            
            log.info("容器已删除: {}", container.getContainerName());
            return true;
        }
        finally
        {
            if (session != null && session.isConnected())
            {
                session.disconnect();
            }
        }
    }
    
    /**
     * 执行简单命令（不需要日志记录）
     */
    private void executeCommandSimple(Session session, String command) throws Exception
    {
        ChannelExec channel = null;
        try
        {
            channel = (ChannelExec) session.openChannel("exec");
            channel.setCommand(command);
            
            InputStream in = channel.getInputStream();
            InputStream err = channel.getErrStream();
            
            channel.connect(COMMAND_TIMEOUT);
            
            // 读取输出
            BufferedReader errReader = new BufferedReader(new InputStreamReader(err, StandardCharsets.UTF_8));
            StringBuilder errorOutput = new StringBuilder();
            String line;
            
            // 等待命令执行完成
            int timeout = 0;
            while (!channel.isClosed() && timeout < COMMAND_TIMEOUT)
            {
                while (errReader.ready())
                {
                    line = errReader.readLine();
                    if (line != null)
                    {
                        errorOutput.append(line).append("\n");
                    }
                }
                Thread.sleep(100);
                timeout += 100;
            }
            
            int exitStatus = channel.getExitStatus();
            
            if (exitStatus != 0)
            {
                String errorMsg = errorOutput.length() > 0 ? errorOutput.toString() : "命令执行失败";
                throw new Exception(errorMsg);
            }
        }
        finally
        {
            if (channel != null && channel.isConnected())
            {
                channel.disconnect();
            }
        }
    }
}
