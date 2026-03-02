package com.ruoyi.web.mapper;

import com.ruoyi.web.domain.DockerImage;
import org.apache.ibatis.annotations.Param;
import java.util.List;

/**
 * Docker镜像缓存Mapper接口
 * 
 * @author ruoyi
 */
public interface DockerImageMapper
{
    /**
     * 查询Docker镜像
     * 
     * @param imageId 镜像ID
     * @return Docker镜像
     */
    public DockerImage selectDockerImageById(Long imageId);

    /**
     * 根据镜像名称、标签和服务器ID查询Docker镜像
     * 
     * @param imageName 镜像名称
     * @param imageTag 镜像标签
     * @param serverId 服务器ID
     * @return Docker镜像
     */
    public DockerImage selectDockerImageByNameTagServer(@Param("imageName") String imageName, 
                                                          @Param("imageTag") String imageTag,
                                                          @Param("serverId") Long serverId);

    /**
     * 查询Docker镜像列表
     * 
     * @param dockerImage Docker镜像
     * @return Docker镜像集合
     */
    public List<DockerImage> selectDockerImageList(DockerImage dockerImage);

    /**
     * 根据服务器ID查询Docker镜像列表
     * 
     * @param serverId 服务器ID
     * @return Docker镜像集合
     */
    public List<DockerImage> selectDockerImageListByServerId(Long serverId);

    /**
     * 新增Docker镜像
     * 
     * @param dockerImage Docker镜像
     * @return 结果
     */
    public int insertDockerImage(DockerImage dockerImage);

    /**
     * 修改Docker镜像
     * 
     * @param dockerImage Docker镜像
     * @return 结果
     */
    public int updateDockerImage(DockerImage dockerImage);

    /**
     * 更新镜像最后使用时间
     * 
     * @param imageId 镜像ID
     * @param lastUsedTime 最后使用时间
     * @return 结果
     */
    public int updateImageLastUsedTime(@Param("imageId") Long imageId, 
                                        @Param("lastUsedTime") java.util.Date lastUsedTime);

    /**
     * 删除Docker镜像
     * 
     * @param imageId 镜像ID
     * @return 结果
     */
    public int deleteDockerImageById(Long imageId);

    /**
     * 批量删除Docker镜像
     * 
     * @param imageIds 需要删除的镜像ID
     * @return 结果
     */
    public int deleteDockerImageByIds(Long[] imageIds);

    /**
     * 删除指定服务器的所有镜像缓存
     * 
     * @param serverId 服务器ID
     * @return 结果
     */
    public int deleteDockerImageByServerId(Long serverId);
}
