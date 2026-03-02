package com.ruoyi.web.mapper;

import com.ruoyi.web.domain.DockerContainer;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

/**
 * Docker容器Mapper接口
 * 
 * @author ruoyi
 */
@Mapper
public interface DockerContainerMapper
{
    /**
     * 查询Docker容器
     * 
     * @param containerId 容器ID
     * @return Docker容器
     */
    public DockerContainer selectDockerContainerById(Long containerId);

    /**
     * 根据容器名称和服务器ID查询Docker容器
     * 
     * @param containerName 容器名称
     * @param serverId 服务器ID
     * @return Docker容器
     */
    public DockerContainer selectDockerContainerByNameAndServer(@Param("containerName") String containerName, 
                                                                  @Param("serverId") Long serverId);

    /**
     * 根据Docker容器ID查询
     * 
     * @param containerDockerId Docker容器ID
     * @return Docker容器
     */
    public DockerContainer selectDockerContainerByDockerId(String containerDockerId);

    /**
     * 查询Docker容器列表
     * 
     * @param dockerContainer Docker容器
     * @return Docker容器集合
     */
    public List<DockerContainer> selectDockerContainerList(DockerContainer dockerContainer);

    /**
     * 根据服务器ID查询Docker容器列表
     * 
     * @param serverId 服务器ID
     * @return Docker容器集合
     */
    public List<DockerContainer> selectDockerContainerListByServerId(Long serverId);

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
     * 更新容器状态
     * 
     * @param containerId 容器ID
     * @param containerStatus 容器状态
     * @return 结果
     */
    public int updateContainerStatus(@Param("containerId") Long containerId, 
                                      @Param("containerStatus") String containerStatus);

    /**
     * 更新容器健康状态
     * 
     * @param containerId 容器ID
     * @param healthStatus 健康状态
     * @param lastHealthCheck 最后健康检查时间
     * @return 结果
     */
    public int updateContainerHealthStatus(@Param("containerId") Long containerId, 
                                            @Param("healthStatus") String healthStatus,
                                            @Param("lastHealthCheck") java.util.Date lastHealthCheck);

    /**
     * 删除Docker容器
     * 
     * @param containerId 容器ID
     * @return 结果
     */
    public int deleteDockerContainerById(Long containerId);

    /**
     * 批量删除Docker容器
     * 
     * @param containerIds 需要删除的容器ID
     * @return 结果
     */
    public int deleteDockerContainerByIds(Long[] containerIds);
}
