package com.ruoyi.web.service.impl;

import java.util.Date;
import java.util.List;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.utils.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.web.mapper.OpsDeployTemplateMapper;
import com.ruoyi.web.domain.OpsDeployTemplate;
import com.ruoyi.web.service.IOpsDeployTemplateService;
import com.ruoyi.web.mapper.OpsDeployTemplateVersionMapper;
import com.ruoyi.web.domain.OpsDeployTemplateVersion;
import com.ruoyi.web.mapper.OpsDeployRecordMapper;
import com.ruoyi.web.domain.OpsDeployRecord;
import com.ruoyi.web.service.IOpsDeployDocService;
import com.ruoyi.system.service.ISysOperLogService;
import com.ruoyi.system.domain.SysOperLog;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import java.io.InputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.nio.charset.StandardCharsets;
import java.util.Properties;
import com.ruoyi.web.domain.OpsApp;
import com.ruoyi.web.mapper.OpsAppMapper;
import com.ruoyi.web.domain.OpsServer;
import com.ruoyi.web.mapper.OpsServerMapper;
import com.alibaba.fastjson2.JSONObject;

import java.util.Map;

import com.ruoyi.common.config.RuoYiConfig;

/**
 * 部署模板Service业务层处理
 * 
 * @author ruoyi
 */
@Service
public class OpsDeployTemplateServiceImpl implements IOpsDeployTemplateService 
{
    @Autowired
    private OpsDeployTemplateMapper opsDeployTemplateMapper;

    @Autowired
    private OpsDeployTemplateVersionMapper opsDeployTemplateVersionMapper;

    @Autowired
    private OpsDeployRecordMapper opsDeployRecordMapper;

    @Autowired
    private IOpsDeployDocService opsDeployDocService;

    @Autowired
    private ISysOperLogService sysOperLogService;
    
    @Autowired
    private OpsAppMapper opsAppMapper;
    
    @Autowired
    private OpsServerMapper opsServerMapper;

    /**
     * 查询部署模板
     * 
     * @param id 部署模板主键
     * @return 部署模板
     */
    @Override
    public OpsDeployTemplate selectOpsDeployTemplateById(Long id)
    {
        return opsDeployTemplateMapper.selectOpsDeployTemplateById(id);
    }

    /**
     * 查询部署模板列表
     * 
     * @param opsDeployTemplate 部署模板
     * @return 部署模板
     */
    @Override
    public List<OpsDeployTemplate> selectOpsDeployTemplateList(OpsDeployTemplate opsDeployTemplate)
    {
        return opsDeployTemplateMapper.selectOpsDeployTemplateList(opsDeployTemplate);
    }

    /**
     * 新增部署模板
     * 
     * @param opsDeployTemplate 部署模板
     * @return 结果
     */
    @Override
    @Transactional
    public int insertOpsDeployTemplate(OpsDeployTemplate opsDeployTemplate)
    {
        opsDeployTemplate.setCreateTime(DateUtils.getNowDate());
        opsDeployTemplate.setCreateBy(SecurityUtils.getUsername());
        opsDeployTemplate.setVersion("v1.0.0");
        int rows = opsDeployTemplateMapper.insertOpsDeployTemplate(opsDeployTemplate);
        
        // Save version
        insertTemplateVersion(opsDeployTemplate, "Initial creation");
        return rows;
    }

    /**
     * 修改部署模板
     * 
     * @param opsDeployTemplate 部署模板
     * @return 结果
     */
    @Override
    @Transactional
    public int updateOpsDeployTemplate(OpsDeployTemplate opsDeployTemplate)
    {
        opsDeployTemplate.setUpdateTime(DateUtils.getNowDate());
        opsDeployTemplate.setUpdateBy(SecurityUtils.getUsername());
        
        // Auto increment version logic
        OpsDeployTemplate oldTemplate = opsDeployTemplateMapper.selectOpsDeployTemplateById(opsDeployTemplate.getId());
        if (oldTemplate != null) {
            String newVersion = incrementVersion(oldTemplate.getVersion());
            opsDeployTemplate.setVersion(newVersion);
            
            // Save new version
            insertTemplateVersion(opsDeployTemplate, "Update template");
            
            // Copy docs to new version
            opsDeployDocService.copyDocsToNewVersion(opsDeployTemplate.getId(), oldTemplate.getVersion(), newVersion);
        }
        
        return opsDeployTemplateMapper.updateOpsDeployTemplate(opsDeployTemplate);
    }

    /**
     * 批量删除部署模板
     * 
     * @param ids 需要删除的部署模板主键
     * @return 结果
     */
    @Override
    public int deleteOpsDeployTemplateByIds(Long[] ids)
    {
        return opsDeployTemplateMapper.deleteOpsDeployTemplateByIds(ids);
    }

    /**
     * 删除部署模板信息
     * 
     * @param id 部署模板主键
     * @return 结果
     */
    @Override
    public int deleteOpsDeployTemplateById(Long id)
    {
        return opsDeployTemplateMapper.deleteOpsDeployTemplateById(id);
    }

    @Override
    public List<OpsDeployTemplateVersion> selectOpsDeployTemplateVersionList(Long templateId) {
        OpsDeployTemplateVersion query = new OpsDeployTemplateVersion();
        query.setTemplateId(templateId);
        return opsDeployTemplateVersionMapper.selectOpsDeployTemplateVersionList(query);
    }

    @Override
    @Transactional
    public int rollbackTemplate(Long templateId, Long versionId) {
        OpsDeployTemplate template = opsDeployTemplateMapper.selectOpsDeployTemplateById(templateId);
        OpsDeployTemplateVersion targetVersion = opsDeployTemplateVersionMapper.selectOpsDeployTemplateVersionById(versionId);
        
        if (template != null && targetVersion != null) {
            String newVersion = incrementVersion(template.getVersion());
            template.setVersion(newVersion);
            template.setScriptContent(targetVersion.getScriptContent());
            template.setSha256(targetVersion.getSha256());
            template.setGuideContent(targetVersion.getGuideContent());
            template.setUpdateBy(SecurityUtils.getUsername());
            template.setUpdateTime(DateUtils.getNowDate());
            
            opsDeployTemplateMapper.updateOpsDeployTemplate(template);
            
            // Insert new version record for the rollback
            OpsDeployTemplateVersion newVersionRecord = new OpsDeployTemplateVersion();
            newVersionRecord.setTemplateId(template.getId());
            newVersionRecord.setVersion(newVersion);
            newVersionRecord.setScriptContent(template.getScriptContent());
            newVersionRecord.setSha256(template.getSha256());
            newVersionRecord.setChangeLog("Rollback to version " + targetVersion.getVersion());
            newVersionRecord.setCreator(SecurityUtils.getUsername());
            newVersionRecord.setCreateTime(DateUtils.getNowDate());
            opsDeployTemplateVersionMapper.insertOpsDeployTemplateVersion(newVersionRecord);
            
            // Copy docs from target version to new version
            opsDeployDocService.copyDocsToNewVersion(template.getId(), targetVersion.getVersion(), newVersion);
            
            return 1;
        }
        return 0;
    }

    @Override
    @Transactional
    public Long deploy(Long templateId, Long appId, Map<String, String> params) {
        OpsDeployTemplate template = opsDeployTemplateMapper.selectOpsDeployTemplateById(templateId);
        if (template == null) {
            throw new RuntimeException("Template not found");
        }
        
        OpsApp app = opsAppMapper.selectOpsAppById(appId);
        if (app == null) {
            throw new RuntimeException("App not found");
        }
        
        // Find host info from app's serverIds (Assuming single server for now or taking first one)
        String serverIds = app.getServerIds();
        if (StringUtils.isEmpty(serverIds)) {
             throw new RuntimeException("App has no associated servers");
        }
        Long serverId = Long.parseLong(serverIds.split(",")[0]);
        
        // Get server info from t_ops_server table
        OpsServer server = opsServerMapper.selectOpsServerByServerId(serverId);
        if (server == null) {
            throw new RuntimeException("Server not found");
        }
        
        // Get host IP (prefer public IP, fallback to private IP)
        String hostIp = StringUtils.isNotEmpty(server.getPublicIp()) ? server.getPublicIp() : server.getPrivateIp();
        if (StringUtils.isEmpty(hostIp)) {
            throw new RuntimeException("Server has no IP address");
        }

        // Find the template version ID corresponding to the current template version
        OpsDeployTemplateVersion query = new OpsDeployTemplateVersion();
        query.setTemplateId(templateId);
        query.setVersion(template.getVersion());
        List<OpsDeployTemplateVersion> versions = opsDeployTemplateVersionMapper.selectOpsDeployTemplateVersionList(query);
        Long versionId = versions.isEmpty() ? null : versions.get(0).getId();

        // Create deployment record
        OpsDeployRecord record = new OpsDeployRecord();
        record.setAppId(appId);
        record.setTemplateVersionId(versionId);
        record.setStartTime(new Date());
        record.setStatus("0"); // Running
        record.setCreateBy(SecurityUtils.getUsername());
        record.setCreateTime(DateUtils.getNowDate());
        opsDeployRecordMapper.insertOpsDeployRecord(record);
        
        // Execute remote deployment asynchronously
        new Thread(() -> {
            try {
                executeRemoteDeploy(server, hostIp, app, template, record, params);
            } catch (Exception e) {
                record.setStatus("1"); // Failed
                record.setErrorMsg(e.getMessage());
                record.setEndTime(new Date());
                // 组装一个简单的失败日志，前端也能看到错误信息
                JSONObject result = new JSONObject();
                result.put("exitCode", -1);
                result.put("output", "Deployment exception: " + e.getMessage());
                record.setJsonResult(result.toJSONString());
                opsDeployRecordMapper.updateOpsDeployRecord(record);
            }
        }).start();
        
        return record.getId();
    }

    private void executeRemoteDeploy(OpsServer server, String hostIp, OpsApp app, OpsDeployTemplate template, OpsDeployRecord record, Map<String, String> params) throws Exception {
        JSch jsch = new JSch();
        Session session = null;
        ChannelExec channel = null;
        
        try {
            // 1. Connect to server using SSH credentials from t_ops_server
            String user = StringUtils.isNotEmpty(server.getUsername()) ? server.getUsername() : "root";
            String password = server.getPassword() != null ? server.getPassword() : "";
            int sshPort = server.getServerPort() != null ? server.getServerPort() : 22;
            
            session = jsch.getSession(user, hostIp, sshPort);
            session.setPassword(password);
            
            Properties config = new Properties();
            config.put("StrictHostKeyChecking", "no");
            session.setConfig(config);
            session.connect(10000);
            
            if (!session.isConnected()) {
                throw new RuntimeException("SSH connection failed: session not connected to " + hostIp);
            }
            
            // 2. Upload Start/Stop scripts and 应用包
            String deployPath = app.getDeployPath();
            if (StringUtils.isEmpty(deployPath)) {
                deployPath = "/opt/apps/" + app.getAppName();
            }

            // 将 /profile 开头的虚拟路径转换为本地真实路径
            String localPackagePath = app.getPackagePath();
            if (StringUtils.isNotEmpty(localPackagePath) && localPackagePath.startsWith("/profile")) {
                // "/profile" 长度为 8
                localPackagePath = RuoYiConfig.getProfile() + localPackagePath.substring(8);
            }

            // 先根据应用包本地路径计算远程包路径（deployPath + 文件名）
            String remotePackagePath = "";
            String packageFileName = null;
            if (StringUtils.isNotEmpty(localPackagePath)) {
                File pkgFile = new File(localPackagePath);
                packageFileName = pkgFile.getName();
                remotePackagePath = deployPath + "/" + packageFileName;
            }
            
            // Ensure directory exists
            ChannelExec mkdirChannel = (ChannelExec) session.openChannel("exec");
            mkdirChannel.setCommand("mkdir -p " + deployPath);
            mkdirChannel.connect();
            while (!mkdirChannel.isClosed()) { Thread.sleep(100); }
            mkdirChannel.disconnect();

            ChannelSftp sftp = (ChannelSftp) session.openChannel("sftp");
            sftp.connect();

            if (StringUtils.isNotEmpty(app.getStartScript())) {
                try (InputStream is = new ByteArrayInputStream(app.getStartScript().getBytes(StandardCharsets.UTF_8))) {
                    sftp.put(is, deployPath + "/start.sh");
                }
            }

            if (StringUtils.isNotEmpty(app.getStopScript())) {
                try (InputStream is = new ByteArrayInputStream(app.getStopScript().getBytes(StandardCharsets.UTF_8))) {
                    sftp.put(is, deployPath + "/stop.sh");
                }
            }

            // 上传应用包到部署目录
            if (StringUtils.isNotEmpty(localPackagePath) && packageFileName != null) {
                File pkgFile = new File(localPackagePath);
                if (pkgFile.exists() && pkgFile.isFile()) {
                    try (InputStream is = new FileInputStream(pkgFile)) {
                        sftp.put(is, remotePackagePath);
                    }
                } else {
                    // 本地找不到包文件时，仍然继续后续流程，但在日志中体现
                    // 这里不抛出异常，避免看不到远程执行日志
                }
            }
            sftp.disconnect();

            // Make scripts executable
            ChannelExec chmodChannel = (ChannelExec) session.openChannel("exec");
            chmodChannel.setCommand("chmod +x " + deployPath + "/*.sh");
            chmodChannel.connect();
            while (!chmodChannel.isClosed()) { Thread.sleep(100); }
            chmodChannel.disconnect();
            
            // 3. Prepare script
            String script = template.getScriptContent();
            // Replace placeholders
            script = script.replace("${app_name}", app.getAppName())
                           .replace("${deploy_path}", deployPath)
                           .replace("${package_path}", remotePackagePath == null ? "" : remotePackagePath);
            
            // Replace dynamic params
            if (params != null) {
                for (Map.Entry<String, String> entry : params.entrySet()) {
                    script = script.replace("${" + entry.getKey() + "}", entry.getValue());
                }
                
                // Add start script parameters to template script
                if (params.containsKey("start_name")) {
                    String startName = params.get("start_name");
                    if (startName.contains(" ")) {
                        // If start_name contains parameters, use it directly
                        script = script.replace("sh start.sh", startName);
                        script = script.replace("./start.sh", startName);
                    } else {
                        // If start_name is just the script name, use it without parameters
                        script = script.replace("sh start.sh", "sh " + startName);
                        script = script.replace("./start.sh", "./" + startName);
                    }
                }
            }
            
            // 4. Execute start script with parameters if start.sh exists
            if (StringUtils.isNotEmpty(app.getStartScript())) {
                // Get start script command from params or use default
                String startScriptCommand = "cd " + deployPath + " && sh start.sh";
                
                // Check if params contains start script parameters
                if (params != null && params.containsKey("start_name")) {
                    String startName = params.get("start_name");
                    if (startName.contains(" ")) {
                        // If start_name contains parameters, use it directly
                        startScriptCommand = "cd " + deployPath + " && " + startName;
                    } else {
                        // If start_name is just the script name, use it without parameters
                        startScriptCommand = "cd " + deployPath + " && sh " + startName;
                    }
                }
                
                ChannelExec startChannel = (ChannelExec) session.openChannel("exec");
                startChannel.setCommand(startScriptCommand);
                
                InputStream startIn = startChannel.getInputStream();
                InputStream startErr = startChannel.getErrStream();
                
                startChannel.connect();
                
                // Read start script output
                StringBuilder startOutput = new StringBuilder();
                byte[] tmp = new byte[1024];
                while (true) {
                    while (startIn.available() > 0) {
                        int i = startIn.read(tmp, 0, 1024);
                        if (i < 0) break;
                        startOutput.append(new String(tmp, 0, i));
                    }
                    while (startErr.available() > 0) {
                        int i = startErr.read(tmp, 0, 1024);
                        if (i < 0) break;
                        startOutput.append(new String(tmp, 0, i));
                    }
                    if (startChannel.isClosed()) {
                        if (startIn.available() > 0) continue;
                        break;
                    }
                    try { Thread.sleep(1000); } catch (Exception ee) {}
                }
                
                startChannel.disconnect();
                
                // Add start script output to overall output
                script = script + "\n# Start script output:\n" + startOutput.toString();
            }
                           
            // 5. Execute deployment template script
            // Write script to remote server and execute it
            String remoteScriptPath = deployPath + "/deploy.sh";
            
            // Reconnect SFTP to upload script
            ChannelSftp sftpDeploy = (ChannelSftp) session.openChannel("sftp");
            sftpDeploy.connect();
            
            // Upload script to remote server
            try (InputStream scriptIs = new ByteArrayInputStream(script.getBytes(StandardCharsets.UTF_8))) {
                sftpDeploy.put(scriptIs, remoteScriptPath);
            }
            
            sftpDeploy.disconnect();
            
            // Make script executable
            ChannelExec chmodScriptChannel = (ChannelExec) session.openChannel("exec");
            chmodScriptChannel.setCommand("chmod +x " + remoteScriptPath);
            chmodScriptChannel.connect();
            while (!chmodScriptChannel.isClosed()) { Thread.sleep(100); }
            chmodScriptChannel.disconnect();
            
            // Execute script
            channel = (ChannelExec) session.openChannel("exec");
            channel.setCommand("cd " + deployPath + " && bash " + remoteScriptPath);
            
            InputStream in = channel.getInputStream();
            InputStream err = channel.getErrStream();
            
            channel.connect();
            
            // Read output
            StringBuilder output = new StringBuilder();
            byte[] tmp = new byte[1024];
            while (true) {
                while (in.available() > 0) {
                    int i = in.read(tmp, 0, 1024);
                    if (i < 0) break;
                    output.append(new String(tmp, 0, i));
                }
                while (err.available() > 0) {
                    int i = err.read(tmp, 0, 1024);
                    if (i < 0) break;
                    output.append(new String(tmp, 0, i));
                }
                if (channel.isClosed()) {
                    if (in.available() > 0) continue;
                    break;
                }
                try { Thread.sleep(1000); } catch (Exception ee) {}
            }
            
            // 4. Update record
            record.setEndTime(new Date());
            record.setDuration(record.getEndTime().getTime() - record.getStartTime().getTime());
            
            JSONObject result = new JSONObject();
            result.put("exitCode", channel.getExitStatus());
            result.put("output", output.toString());
            record.setJsonResult(result.toJSONString());
            
            if (channel.getExitStatus() == 0) {
                record.setStatus("2"); // Success
            } else {
                record.setStatus("1"); // Failed
                record.setErrorMsg("Exit code: " + channel.getExitStatus());
            }
            
            opsDeployRecordMapper.updateOpsDeployRecord(record);
            
        } finally {
            if (channel != null) channel.disconnect();
            if (session != null) session.disconnect();
        }
    }

    private void insertTemplateVersion(OpsDeployTemplate template, String changeLog) {
        OpsDeployTemplateVersion version = new OpsDeployTemplateVersion();
        version.setTemplateId(template.getId());
        version.setVersion(template.getVersion());
        version.setScriptContent(template.getScriptContent());
        version.setSha256(template.getSha256());
        version.setGuideContent(template.getGuideContent());
        version.setChangeLog(changeLog);
        version.setCreator(template.getUpdateBy() == null ? template.getCreateBy() : template.getUpdateBy());
        version.setCreateTime(DateUtils.getNowDate());
        opsDeployTemplateVersionMapper.insertOpsDeployTemplateVersion(version);
    }

    private String incrementVersion(String version) {
        if (StringUtils.isEmpty(version)) return "v1.0.0";
        String[] parts = version.replace("v", "").split("\\.");
        if (parts.length < 3) return "v1.0.1";
        int major = Integer.parseInt(parts[0]);
        int minor = Integer.parseInt(parts[1]);
        int patch = Integer.parseInt(parts[2]);
        patch++;
        return "v" + major + "." + minor + "." + patch;
    }
}
