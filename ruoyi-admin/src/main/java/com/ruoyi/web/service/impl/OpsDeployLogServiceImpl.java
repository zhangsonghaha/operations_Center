package com.ruoyi.web.service.impl;

import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.web.domain.OpsApp;
import com.ruoyi.web.domain.OpsDeployLog;
import com.ruoyi.web.mapper.OpsAppMapper;
import com.ruoyi.web.mapper.OpsDeployLogMapper;
import com.ruoyi.web.service.IDeployService;
import com.ruoyi.web.service.IOpsDeployLogService;
import com.ruoyi.web.websocket.DeployLogWebSocket;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

/**
 * 部署日志Service业务层处理
 * 
 * @author ruoyi
 */
@Service
public class OpsDeployLogServiceImpl implements IOpsDeployLogService {
    
    @Autowired
    private OpsDeployLogMapper opsDeployLogMapper;
    
    @Autowired
    private OpsAppMapper opsAppMapper;
    
    @Autowired
    private IDeployService deployService;
    
    @Override
    public OpsDeployLog selectOpsDeployLogById(Long logId) {
        return opsDeployLogMapper.selectOpsDeployLogById(logId);
    }
    
    @Override
    public List<OpsDeployLog> selectOpsDeployLogList(OpsDeployLog opsDeployLog) {
        return opsDeployLogMapper.selectOpsDeployLogList(opsDeployLog);
    }
    
    @Override
    public int insertOpsDeployLog(OpsDeployLog opsDeployLog) {
        opsDeployLog.setCreateTime(DateUtils.getNowDate());
        return opsDeployLogMapper.insertOpsDeployLog(opsDeployLog);
    }
    
    @Override
    public int updateOpsDeployLog(OpsDeployLog opsDeployLog) {
        opsDeployLog.setUpdateTime(DateUtils.getNowDate());
        return opsDeployLogMapper.updateOpsDeployLog(opsDeployLog);
    }
    
    @Override
    public int deleteOpsDeployLogByIds(Long[] logIds) {
        return opsDeployLogMapper.deleteOpsDeployLogByIds(logIds);
    }
    
    @Override
    public int deleteOpsDeployLogById(Long logId) {
        return opsDeployLogMapper.deleteOpsDeployLogById(logId);
    }
    
    @Override
    public Long startDeploy(Long appId, Long serverId, String deployType) {
        OpsApp app = opsAppMapper.selectOpsAppById(appId);
        
        OpsDeployLog log = new OpsDeployLog();
        log.setAppId(appId);
        log.setAppName(app != null ? app.getAppName() : "");
        log.setServerId(serverId);
        log.setDeployType(deployType);
        log.setDeployStatus("running");
        log.setStartTime(new Date());
        log.setExecutor(SecurityUtils.getUsername());
        log.setLogContent("");
        
        insertOpsDeployLog(log);
        
        // 推送初始消息
        String initMsg = String.format("[%s] 开始%s应用: %s\n", 
            DateUtils.dateTimeNow(), getDeployTypeName(deployType), app.getAppName());
        appendLog(log.getLogId(), initMsg);
        
        // 异步执行部署
        deployService.executeDeploy(log.getLogId(), appId, serverId, deployType);
        
        return log.getLogId();
    }
    
    @Override
    public void appendLog(Long logId, String content) {
        // 使用数据库 CONCAT 直接追加，避免读取整个日志内容
        opsDeployLogMapper.appendLogContent(logId, content, DateUtils.getNowDate());
        
        // 通过WebSocket推送日志
        DeployLogWebSocket.sendMessage(logId.toString(), content);
    }
    
    @Override
    public void finishDeploy(Long logId, boolean success, String errorMsg) {
        // 创建一个新对象，只设置需要更新的字段
        OpsDeployLog log = new OpsDeployLog();
        log.setLogId(logId);
        log.setDeployStatus(success ? "success" : "failed");
        log.setEndTime(new Date());
        log.setErrorMsg(errorMsg);
        log.setUpdateTime(DateUtils.getNowDate());
        
        // 只更新状态字段，不读取和更新日志内容
        updateOpsDeployLog(log);
        
        // 使用 appendLogContent 追加完成消息
        String finishMsg = String.format("\n[%s] %s%s\n", 
            DateUtils.dateTimeNow(), 
            success ? "部署成功" : "部署失败",
            errorMsg != null ? ": " + errorMsg : "");
        
        opsDeployLogMapper.appendLogContent(logId, finishMsg, DateUtils.getNowDate());
        
        // 推送完成消息
        DeployLogWebSocket.sendMessage(logId.toString(), finishMsg);
    }
    
    private String getDeployTypeName(String deployType) {
        switch (deployType) {
            case "deploy": return "部署";
            case "start": return "启动";
            case "stop": return "停止";
            case "restart": return "重启";
            default: return deployType;
        }
    }
}
