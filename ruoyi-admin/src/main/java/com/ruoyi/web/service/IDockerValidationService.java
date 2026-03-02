package com.ruoyi.web.service;

import com.jcraft.jsch.Session;
import com.ruoyi.web.domain.dto.VolumeMount;
import java.util.List;

/**
 * Docker部署前验证Service接口
 * 
 * @author ruoyi
 */
public interface IDockerValidationService
{
    /**
     * 验证容器名称格式
     * 
     * @param containerName 容器名称
     * @throws IllegalArgumentException 如果名称格式不正确
     */
    public void validateContainerName(String containerName);

    /**
     * 检查容器名称是否已存在
     * 
     * @param session SSH会话
     * @param containerName 容器名称
     * @return true-已存在, false-不存在
     */
    public boolean checkContainerNameExists(Session session, String containerName);

    /**
     * 检查端口是否被占用
     * 
     * @param session SSH会话
     * @param port 端口号
     * @return true-被占用, false-未占用
     */
    public boolean checkPortInUse(Session session, int port);

    /**
     * 验证卷挂载路径安全性
     * 
     * @param volumes 卷挂载列表
     * @throws SecurityException 如果包含敏感路径
     */
    public void validateVolumeMounts(List<VolumeMount> volumes);

    /**
     * 验证资源限制
     * 
     * @param cpuLimit CPU限制（核数）
     * @param memoryLimit 内存限制（如: 2g, 512m）
     * @throws IllegalArgumentException 如果资源限制不合法
     */
    public void validateResourceLimits(java.math.BigDecimal cpuLimit, String memoryLimit);
}
