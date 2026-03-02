package com.ruoyi.web.service;

import com.ruoyi.web.domain.dto.DockerDeployConfig;
import com.ruoyi.web.domain.dto.DockerDeployResult;

/**
 * Docker部署服务接口
 * 
 * @author ruoyi
 */
public interface IDockerDeployService
{
    /**
     * 部署Docker容器
     * 执行完整的部署流程：
     * 1. 环境检测
     * 2. 部署前验证
     * 3. 镜像拉取
     * 4. 容器创建和启动
     * 5. 健康检查
     * 6. 保存容器记录
     * 
     * @param config 部署配置
     * @return 部署结果
     * @throws Exception 部署失败时抛出异常
     */
    DockerDeployResult deployContainer(DockerDeployConfig config) throws Exception;

    /**
     * 停止Docker容器
     * 
     * @param containerId 容器ID
     * @param serverId 服务器ID
     * @return 是否成功
     * @throws Exception 操作失败时抛出异常
     */
    boolean stopContainer(Long containerId, Long serverId) throws Exception;

    /**
     * 启动Docker容器
     * 
     * @param containerId 容器ID
     * @param serverId 服务器ID
     * @return 是否成功
     * @throws Exception 操作失败时抛出异常
     */
    boolean startContainer(Long containerId, Long serverId) throws Exception;

    /**
     * 重启Docker容器
     * 
     * @param containerId 容器ID
     * @param serverId 服务器ID
     * @return 是否成功
     * @throws Exception 操作失败时抛出异常
     */
    boolean restartContainer(Long containerId, Long serverId) throws Exception;

    /**
     * 删除Docker容器
     * 
     * @param containerId 容器ID
     * @param serverId 服务器ID
     * @param force 是否强制删除
     * @return 是否成功
     * @throws Exception 操作失败时抛出异常
     */
    boolean removeContainer(Long containerId, Long serverId, boolean force) throws Exception;
}
