package com.ruoyi.web.service.impl;

import java.io.InputStream;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicInteger;

import com.jcraft.jsch.*;
import com.ruoyi.common.config.RuoYiConfig;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.web.domain.OpsServer;
import com.ruoyi.web.domain.SysBatchTask;
import com.ruoyi.web.domain.SysBatchTaskDetail;
import com.ruoyi.web.domain.SysCommandTemplate;
import com.ruoyi.web.mapper.SysBatchTaskDetailMapper;
import com.ruoyi.web.mapper.SysBatchTaskMapper;
import com.ruoyi.web.mapper.SysCommandTemplateMapper;
import com.ruoyi.web.service.IOpsServerService;
import com.ruoyi.web.service.ISysBatchService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 批量操作Service业务层处理
 */
@Service
public class SysBatchServiceImpl implements ISysBatchService 
{
    private static final Logger log = LoggerFactory.getLogger(SysBatchServiceImpl.class);

    @Autowired
    private SysCommandTemplateMapper sysCommandTemplateMapper;

    @Autowired
    private SysBatchTaskMapper sysBatchTaskMapper;

    @Autowired
    private SysBatchTaskDetailMapper sysBatchTaskDetailMapper;

    @Autowired
    private IOpsServerService opsServerService;

    @Autowired
    @Qualifier("threadPoolTaskExecutor")
    private Executor threadPoolTaskExecutor;

    @Override
    public List<SysCommandTemplate> selectSysCommandTemplateList(SysCommandTemplate sysCommandTemplate)
    {
        return sysCommandTemplateMapper.selectSysCommandTemplateList(sysCommandTemplate);
    }

    @Override
    public int insertSysCommandTemplate(SysCommandTemplate sysCommandTemplate)
    {
        sysCommandTemplate.setCreateTime(DateUtils.getNowDate());
        return sysCommandTemplateMapper.insertSysCommandTemplate(sysCommandTemplate);
    }

    @Override
    public int updateSysCommandTemplate(SysCommandTemplate sysCommandTemplate)
    {
        sysCommandTemplate.setUpdateTime(DateUtils.getNowDate());
        return sysCommandTemplateMapper.updateSysCommandTemplate(sysCommandTemplate);
    }

    @Override
    public int deleteSysCommandTemplateByIds(Long[] templateIds)
    {
        return sysCommandTemplateMapper.deleteSysCommandTemplateByIds(templateIds);
    }

    @Override
    public List<SysBatchTask> selectSysBatchTaskList(SysBatchTask sysBatchTask)
    {
        return sysBatchTaskMapper.selectSysBatchTaskList(sysBatchTask);
    }

    @Override
    public SysBatchTask selectSysBatchTaskById(Long taskId)
    {
        SysBatchTask task = sysBatchTaskMapper.selectSysBatchTaskById(taskId);
        task.setTaskDetailList(sysBatchTaskDetailMapper.selectSysBatchTaskDetailListByTaskId(taskId));
        return task;
    }

    @Override
    public int insertSysBatchTask(SysBatchTask sysBatchTask)
    {
        sysBatchTask.setCreateTime(DateUtils.getNowDate());
        return sysBatchTaskMapper.insertSysBatchTask(sysBatchTask);
    }

    @Override
    public List<SysBatchTaskDetail> selectSysBatchTaskDetailList(SysBatchTaskDetail sysBatchTaskDetail)
    {
        return sysBatchTaskDetailMapper.selectSysBatchTaskDetailList(sysBatchTaskDetail);
    }

    /**
     * 执行批量任务
     */
    @Override
    @Transactional
    public void executeBatchTask(Long taskId, Long[] serverIds)
    {
        SysBatchTask task = sysBatchTaskMapper.selectSysBatchTaskById(taskId);
        if (task == null) return;

        // 1. 更新主任务状态
        task.setStatus("1"); // 执行中
        task.setStartTime(DateUtils.getNowDate());
        task.setTotalHost(serverIds.length);
        task.setSuccessHost(0);
        task.setFailHost(0);
        sysBatchTaskMapper.updateSysBatchTask(task);

        // 用于统计完成数
        AtomicInteger completedCount = new AtomicInteger(0);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);

        // 2. 遍历服务器，创建明细并提交异步任务
        for (Long serverId : serverIds) {
            OpsServer server = opsServerService.selectOpsServerByServerId(serverId);
            
            SysBatchTaskDetail detail = new SysBatchTaskDetail();
            detail.setTaskId(taskId);
            detail.setServerId(serverId);
            if (server != null) {
                detail.setServerName(server.getServerName());
                detail.setServerIp(StringUtils.isNotEmpty(server.getPublicIp()) ? server.getPublicIp() : server.getPrivateIp());
            } else {
                detail.setServerName("Unknown");
                detail.setServerIp("Unknown");
            }
            detail.setStatus("0"); // 等待中
            sysBatchTaskDetailMapper.insertSysBatchTaskDetail(detail);

            // 提交异步任务
            threadPoolTaskExecutor.execute(() -> {
                executeSingleServerTask(task, detail, server, completedCount, successCount, failCount, serverIds.length);
            });
        }
    }

    /**
     * 执行单个服务器的任务
     */
    private void executeSingleServerTask(SysBatchTask task, SysBatchTaskDetail detail, OpsServer server, 
                                         AtomicInteger completedCount, AtomicInteger successCount, AtomicInteger failCount, int total) {
        
        detail.setStartTime(DateUtils.getNowDate());
        detail.setStatus("1"); // 执行中
        sysBatchTaskDetailMapper.updateSysBatchTaskDetail(detail);

        StringBuilder logBuffer = new StringBuilder();
        boolean isSuccess = false;

        if (server == null) {
            logBuffer.append("Server not found or deleted.");
            detail.setExitCode(-1);
        } else {
            String ip = StringUtils.isNotEmpty(server.getPublicIp()) ? server.getPublicIp() : server.getPrivateIp();
            int port = server.getServerPort() != null ? server.getServerPort() : 22;

            Session session = null;
            try {
                JSch jsch = new JSch();
                if ("1".equals(server.getAuthType()) && StringUtils.isNotEmpty(server.getPrivateKey())) {
                    jsch.addIdentity("key", server.getPrivateKey().getBytes(), null, null);
                }

                session = jsch.getSession(server.getUsername(), ip, port);
                if ("0".equals(server.getAuthType())) {
                    session.setPassword(server.getPassword());
                }
                
                session.setConfig("StrictHostKeyChecking", "no");
                session.setConfig("PreferredAuthentications", "publickey,password");
                session.connect(10000); // 10s 连接超时

                if ("1".equals(task.getTaskType())) {
                    // 执行命令
                    isSuccess = executeCommand(session, task.getCommandContent(), logBuffer, detail);
                } else if ("2".equals(task.getTaskType())) {
                    // 分发文件
                    String localPath = task.getSourceFile();
                    if (StringUtils.isNotEmpty(localPath) && localPath.startsWith("/profile")) {
                         localPath = RuoYiConfig.getProfile() + localPath.substring(8);
                    }
                    isSuccess = uploadFile(session, localPath, task.getDestPath(), logBuffer);
                }

            } catch (Exception e) {
                logBuffer.append("Connection/Execution Error: ").append(e.getMessage());
                log.error("Task execution failed for server " + ip, e);
            } finally {
                if (session != null && session.isConnected()) {
                    session.disconnect();
                }
            }
        }

        // 更新明细状态
        detail.setEndTime(DateUtils.getNowDate());
        detail.setExecutionLog(logBuffer.toString());
        detail.setStatus(isSuccess ? "2" : "3");
        if (isSuccess) {
            successCount.incrementAndGet();
        } else {
            failCount.incrementAndGet();
        }
        sysBatchTaskDetailMapper.updateSysBatchTaskDetail(detail);

        // 检查是否所有任务完成
        if (completedCount.incrementAndGet() == total) {
            // 更新主任务最终状态
            SysBatchTask finalTask = new SysBatchTask();
            finalTask.setTaskId(task.getTaskId());
            finalTask.setEndTime(DateUtils.getNowDate());
            finalTask.setSuccessHost(successCount.get());
            finalTask.setFailHost(failCount.get());
            if (failCount.get() == 0) {
                finalTask.setStatus("2"); // 成功
            } else if (successCount.get() == 0) {
                finalTask.setStatus("3"); // 失败
            } else {
                finalTask.setStatus("4"); // 部分成功
            }
            sysBatchTaskMapper.updateSysBatchTask(finalTask);
        }
    }

    private boolean executeCommand(Session session, String command, StringBuilder logBuffer, SysBatchTaskDetail detail) {
        ChannelExec channel = null;
        try {
            channel = (ChannelExec) session.openChannel("exec");
            channel.setCommand(command);
            channel.setInputStream(null);
            
            InputStream in = channel.getInputStream();
            InputStream err = channel.getErrStream();
            
            channel.connect();

            byte[] tmp = new byte[1024];
            while (true) {
                while (in.available() > 0) {
                    int i = in.read(tmp, 0, 1024);
                    if (i < 0) break;
                    logBuffer.append(new String(tmp, 0, i));
                }
                while (err.available() > 0) {
                    int i = err.read(tmp, 0, 1024);
                    if (i < 0) break;
                    logBuffer.append("ERR: ").append(new String(tmp, 0, i));
                }
                if (channel.isClosed()) {
                    if (in.available() > 0) continue;
                    int exitStatus = channel.getExitStatus();
                    detail.setExitCode(exitStatus);
                    return exitStatus == 0;
                }
                try { Thread.sleep(100); } catch (Exception ee) {}
            }

        } catch (Exception e) {
            logBuffer.append("Command Error: ").append(e.getMessage());
            return false;
        } finally {
            if (channel != null) channel.disconnect();
        }
    }

    private boolean uploadFile(Session session, String sourceFile, String destPath, StringBuilder logBuffer) {
        ChannelSftp channel = null;
        try {
            channel = (ChannelSftp) session.openChannel("sftp");
            channel.connect();
            
            logBuffer.append("Uploading ").append(sourceFile).append(" to ").append(destPath).append("...\n");
            channel.put(sourceFile, destPath, ChannelSftp.OVERWRITE);
            logBuffer.append("Upload successful.\n");
            
            return true;
        } catch (Exception e) {
            logBuffer.append("Upload Error: ").append(e.getMessage());
            return false;
        } finally {
            if (channel != null) channel.disconnect();
        }
    }
}
