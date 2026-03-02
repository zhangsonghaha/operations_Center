package com.ruoyi.web.service;

import com.ruoyi.web.domain.DockerContainer;
import com.ruoyi.web.domain.dto.DockerContainerStats;

import java.util.List;

/**
 * Docker容器Service接口
 * 
 * @author ruoyi
 */
public interface IDockerContainerService
{
    /**
     * 查询Docker容器
     * 
     * @param containerId Docker容器主键
     * @return Docker容器
     */
    public DockerContainer selectDockerContainerById(Long containerId);

    /**
     * 查询Docker容器列表
     * 
     * @param dockerContainer Docker容器
     * @return Docker容器集合
     */
    public List<DockerContainer> selectDockerContainerList(DockerContainer dockerContainer);

    /**
     * 新增Docker容器
     * 
     * @param dockerContainer Docker容器
     * @return 结果
     */
    public int insertDockerContainer(DockerContainer dockerContainer);

    /**
     * 修改Docker容器
     * 
     * @param dockerContainer Docker容器
     * @return 结果
     */
    public int updateDockerContainer(DockerContainer dockerContainer);

    /**
     * 批量删除Docker容器
     * 
     * @param containerIds 需要删除的Docker容器主键集合
     * @return 结果
     */
    public int deleteDockerContainerByIds(Long[] containerIds);

    /**
     * 删除Docker容器信息
     * 
     * @param containerId Docker容器主键
     * @return 结果
     */
    public int deleteDockerContainerById(Long containerId);

    /**
     * 获取容器日志
     * 
     * @param containerId 容器ID
     * @param tail 返回行数
     * @return 日志内容
     * @throws Exception 获取失败时抛出异常
     */
    public String getContainerLogs(Long containerId, Integer tail) throws Exception;

    /**
     * 获取容器统计信息
     * 
     * @param containerId 容器ID
     * @return 统计信息
     * @throws Exception 获取失败时抛出异常
     */
    public DockerContainerStats getContainerStats(Long containerId) throws Exception;
}
