package com.ruoyi.web.service;

import com.jcraft.jsch.Session;

/**
 * Docker镜像服务接口
 * 
 * @author ruoyi
 */
public interface IDockerImageService
{
    /**
     * 检查镜像是否存在
     * 先查询数据库缓存，如果不存在则通过SSH执行docker images命令检查
     * 
     * @param session SSH会话
     * @param imageName 镜像名称
     * @param imageTag 镜像标签
     * @param serverId 服务器ID
     * @return 镜像是否存在
     * @throws Exception 检查失败时抛出异常
     */
    boolean checkImageExists(Session session, String imageName, String imageTag, Long serverId) throws Exception;

    /**
     * 拉取Docker镜像并实时推送进度
     * 如果镜像已存在，跳过拉取
     * 拉取失败时自动重试（最多3次）
     * 拉取成功后更新镜像缓存表
     * 
     * @param session SSH会话
     * @param imageName 镜像名称
     * @param imageTag 镜像标签
     * @param serverId 服务器ID
     * @param logId 部署日志ID（用于推送进度日志）
     * @return 拉取是否成功
     * @throws Exception 拉取失败时抛出异常
     */
    boolean pullImageWithProgress(Session session, String imageName, String imageTag, Long serverId, Long logId) throws Exception;

    /**
     * 更新镜像缓存记录
     * 如果镜像不存在则插入，存在则更新拉取时间
     * 
     * @param imageName 镜像名称
     * @param imageTag 镜像标签
     * @param serverId 服务器ID
     * @param imageSize 镜像大小（字节）
     * @throws Exception 更新失败时抛出异常
     */
    void updateImageCache(String imageName, String imageTag, Long serverId, Long imageSize) throws Exception;
}
