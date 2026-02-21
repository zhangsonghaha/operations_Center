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
import java.nio.charset.StandardCharsets;
import java.util.Properties;
import com.ruoyi.web.domain.OpsApp;
import com.ruoyi.web.mapper.OpsAppMapper;
import com.ruoyi.web.domain.OpsHost;
import com.ruoyi.web.mapper.OpsDashboardMapper;
import com.alibaba.fastjson2.JSONObject;

import java.util.Map;

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
    private OpsDashboardMapper opsDashboardMapper;

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
        Long hostId = Long.parseLong(serverIds.split(",")[0]);
        OpsHost host = new OpsHost();
        host.setHostId(hostId);
        List<OpsHost> hosts = opsDashboardMapper.selectOpsHostList(host);
        if (hosts.isEmpty()) {
            throw new RuntimeException("Host not found");
        }
        OpsHost targetHost = hosts.get(0);

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
                executeRemoteDeploy(targetHost, app, template, record, params);
            } catch (Exception e) {
                record.setStatus("1"); // Failed
                record.setErrorMsg(e.getMessage());
                record.setEndTime(new Date());
                opsDeployRecordMapper.updateOpsDeployRecord(record);
            }
        }).start();
        
        return record.getId();
    }

    private void executeRemoteDeploy(OpsHost host, OpsApp app, OpsDeployTemplate template, OpsDeployRecord record, Map<String, String> params) throws Exception {
        JSch jsch = new JSch();
        Session session = null;
        ChannelExec channel = null;
        
        try {
            // 1. Connect to server
            // Note: OpsHost needs username/password fields, assuming they exist or using defaults/keys
            // For this implementation, we'll assume standard 'root' and a placeholder password if not in DB
            // In a real system, these should be securely stored in OpsHost
            String user = "root"; 
            String password = "password"; // Placeholder, should be from host.getPassword()
            
            session = jsch.getSession(user, host.getIpAddress(), 22);
            session.setPassword(password);
            
            Properties config = new Properties();
            config.put("StrictHostKeyChecking", "no");
            session.setConfig(config);
            session.connect(10000);
            
            // 2. Upload Start/Stop scripts
            String deployPath = app.getDeployPath();
            if (StringUtils.isEmpty(deployPath)) {
                deployPath = "/opt/apps/" + app.getAppName();
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
                           .replace("${deploy_path}", app.getDeployPath() == null ? "/opt/apps" : app.getDeployPath())
                           .replace("${package_path}", app.getPackagePath() == null ? "" : app.getPackagePath());
            
            // Replace dynamic params
            if (params != null) {
                for (Map.Entry<String, String> entry : params.entrySet()) {
                    script = script.replace("${" + entry.getKey() + "}", entry.getValue());
                }
            }
                           
            // 3. Execute script
            channel = (ChannelExec) session.openChannel("exec");
            channel.setCommand(script);
            
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
