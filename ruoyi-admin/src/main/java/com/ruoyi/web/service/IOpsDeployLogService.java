package com.ruoyi.web.service;

import com.ruoyi.web.domain.OpsDeployLog;
import java.util.List;

/**
 * 部署日志Service接口
 * 
 * @author ruoyi
 */
public interface IOpsDeployLogService {
    
    /**
     * 查询部署日志
     */
    public OpsDeployLog selectOpsDeployLogById(Long logId);
    
    /**
     * 查询部署日志列表
     */
    public List<OpsDeployLog> selectOpsDeployLogList(OpsDeployLog opsDeployLog);
    
    /**
     * 新增部署日志
     */
    public int insertOpsDeployLog(OpsDeployLog opsDeployLog);
    
    /**
     * 修改部署日志
     */
    public int updateOpsDeployLog(OpsDeployLog opsDeployLog);
    
    /**
     * 批量删除部署日志
     */
    public int deleteOpsDeployLogByIds(Long[] logIds);
    
    /**
     * 删除部署日志
     */
    public int deleteOpsDeployLogById(Long logId);
    
    /**
     * 开始部署并创建日志
     */
    public Long startDeploy(Long appId, Long serverId, String deployType);
    
    /**
     * 追加日志内容
     */
    public void appendLog(Long logId, String content);
    
    /**
     * 完成部署
     */
    public void finishDeploy(Long logId, boolean success, String errorMsg);
}
