package com.ruoyi.web.service;

/**
 * 部署服务接口
 * 
 * @author ruoyi
 */
public interface IDeployService {
    
    /**
     * 执行部署
     * 
     * @param logId 日志ID
     * @param appId 应用ID
     * @param serverId 服务器ID
     * @param deployType 部署类型
     */
    void executeDeploy(Long logId, Long appId, Long serverId, String deployType);
}
