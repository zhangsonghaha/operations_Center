package com.ruoyi.web.service;

import com.jcraft.jsch.Session;
import com.ruoyi.web.domain.dto.DockerEnvInfo;

/**
 * Docker环境检测Service接口
 * 
 * @author ruoyi
 */
public interface IDockerEnvService
{
    /**
     * 检查Docker环境
     * 
     * @param serverId 服务器ID
     * @return Docker环境信息
     */
    public DockerEnvInfo checkDockerEnvironment(Long serverId);

    /**
     * 检查Docker环境（使用已有Session）
     * 
     * @param session SSH会话
     * @return Docker环境信息
     */
    public DockerEnvInfo checkDockerEnvironment(Session session);
}
