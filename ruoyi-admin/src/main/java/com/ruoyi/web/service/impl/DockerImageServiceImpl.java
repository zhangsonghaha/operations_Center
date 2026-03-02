package com.ruoyi.web.service.impl;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.Session;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.web.domain.DockerImage;

import com.ruoyi.web.mapper.DockerImageMapper;
import com.ruoyi.web.service.IDockerImageService;
import com.ruoyi.web.service.IOpsDeployLogService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

/**
 * Docker镜像服务实现
 * 
 * @author ruoyi
 */
@Service
public class DockerImageServiceImpl implements IDockerImageService
{
    private static final Logger log = LoggerFactory.getLogger(DockerImageServiceImpl.class);
    
    /** 最大重试次数 */
    private static final int MAX_RETRY_COUNT = 3;
    
    /** 命令执行超时时间（毫秒） */
    private static final int COMMAND_TIMEOUT = 5000;
    
    /** 镜像拉取超时时间（毫秒） */
    private static final int PULL_TIMEOUT = 600000; // 10分钟
    
    @Autowired
    private DockerImageMapper dockerImageMapper;
    
    @Autowired
    private IOpsDeployLogService deployLogService;
    
    /**
     * 检查镜像是否存在
     */
    @Override
    public boolean checkImageExists(Session session, String imageName, String imageTag, Long serverId) throws Exception
    {
        if (StringUtils.isEmpty(imageName))
        {
            throw new IllegalArgumentException("镜像名称不能为空");
        }
        
        // 默认标签为 latest
        if (StringUtils.isEmpty(imageTag))
        {
            imageTag = "latest";
        }
        
        // 1. 先查询数据库缓存
        DockerImage cachedImage = dockerImageMapper.selectDockerImageByNameTagServer(imageName, imageTag, serverId);
        if (cachedImage != null)
        {
            log.info("镜像在缓存中找到: {}:{}", imageName, imageTag);
            return true;
        }
        
        // 2. 通过 SSH 执行 docker images 命令检查
        String fullImageName = imageName + ":" + imageTag;
        String command = "docker images -q " + fullImageName;
        
        log.info("执行命令检查镜像: {}", command);
        
        ChannelExec channel = null;
        try
        {
            channel = (ChannelExec) session.openChannel("exec");
            channel.setCommand(command);
            
            InputStream in = channel.getInputStream();
            channel.connect(COMMAND_TIMEOUT);
            
            // 读取输出
            BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
            String imageId = reader.readLine();
            
            // 等待命令执行完成
            int timeout = 0;
            while (!channel.isClosed() && timeout < COMMAND_TIMEOUT)
            {
                Thread.sleep(100);
                timeout += 100;
            }
            
            boolean exists = StringUtils.isNotEmpty(imageId);
            log.info("镜像 {}:{} 存在性检查结果: {}", imageName, imageTag, exists);
            
            return exists;
        }
        finally
        {
            if (channel != null && channel.isConnected())
            {
                channel.disconnect();
            }
        }
    }
    
    /**
     * 拉取Docker镜像并实时推送进度
     */
    @Override
    public boolean pullImageWithProgress(Session session, String imageName, String imageTag, Long serverId, Long logId) throws Exception
    {
        if (StringUtils.isEmpty(imageName))
        {
            throw new IllegalArgumentException("镜像名称不能为空");
        }
        
        // 默认标签为 latest
        if (StringUtils.isEmpty(imageTag))
        {
            imageTag = "latest";
        }
        
        String fullImageName = imageName + ":" + imageTag;
        
        // 1. 检查镜像是否已存在
        if (checkImageExists(session, imageName, imageTag, serverId))
        {
            deployLogService.appendLog(logId, String.format("[INFO] 镜像已存在，跳过拉取: %s\n", fullImageName));
            
            // 更新最后使用时间
            DockerImage cachedImage = dockerImageMapper.selectDockerImageByNameTagServer(imageName, imageTag, serverId);
            if (cachedImage != null)
            {
                dockerImageMapper.updateImageLastUsedTime(cachedImage.getImageId(), new Date());
            }
            
            return true;
        }
        
        // 2. 执行镜像拉取（带重试）
        int retryCount = 0;
        Exception lastException = null;
        
        while (retryCount < MAX_RETRY_COUNT)
        {
            try
            {
                if (retryCount > 0)
                {
                    deployLogService.appendLog(logId, String.format("[INFO] 第 %d 次重试拉取镜像...\n", retryCount));
                }
                
                boolean success = executePullCommand(session, fullImageName, logId);
                
                if (success)
                {
                    // 3. 拉取成功，更新镜像缓存
                    updateImageCache(imageName, imageTag, serverId, null);
                    deployLogService.appendLog(logId, String.format("[SUCCESS] 镜像拉取成功: %s\n", fullImageName));
                    return true;
                }
                else
                {
                    throw new Exception("镜像拉取失败");
                }
            }
            catch (Exception e)
            {
                lastException = e;
                retryCount++;
                
                if (retryCount < MAX_RETRY_COUNT)
                {
                    deployLogService.appendLog(logId, String.format("[WARN] 镜像拉取失败: %s，准备重试...\n", e.getMessage()));
                    Thread.sleep(2000); // 等待2秒后重试
                }
            }
        }
        
        // 所有重试都失败
        String errorMsg = String.format("镜像拉取失败（已重试%d次）: %s", MAX_RETRY_COUNT, 
            lastException != null ? lastException.getMessage() : "未知错误");
        deployLogService.appendLog(logId, String.format("[ERROR] %s\n", errorMsg));
        throw new Exception(errorMsg);
    }
    
    /**
     * 执行镜像拉取命令并实时推送进度
     */
    private boolean executePullCommand(Session session, String fullImageName, Long logId) throws Exception
    {
        String command = "docker pull " + fullImageName;
        deployLogService.appendLog(logId, String.format("[INFO] 执行命令: %s\n", command));
        
        ChannelExec channel = null;
        try
        {
            channel = (ChannelExec) session.openChannel("exec");
            channel.setCommand(command);
            
            // 合并标准输出和错误输出
            channel.setErrStream(System.err);
            InputStream in = channel.getInputStream();
            
            channel.connect(COMMAND_TIMEOUT);
            
            // 实时读取输出并推送进度
            BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
            Set<String> pushedLines = new HashSet<>(); // 避免推送重复的进度行
            String line;
            int idleCount = 0;
            int maxIdleCount = PULL_TIMEOUT / 100; // 根据超时时间计算最大空闲次数
            
            while (true)
            {
                // 尝试读取一行
                if (reader.ready())
                {
                    line = reader.readLine();
                    if (line != null)
                    {
                        // 过滤重复的进度行（Docker pull 会输出很多重复的进度）
                        String normalizedLine = normalizePullProgress(line);
                        if (!pushedLines.contains(normalizedLine))
                        {
                            deployLogService.appendLog(logId, line + "\n");
                            pushedLines.add(normalizedLine);
                        }
                        
                        idleCount = 0; // 重置空闲计数
                    }
                }
                
                // 检查通道是否关闭
                if (channel.isClosed())
                {
                    // 读取剩余输出
                    while (reader.ready())
                    {
                        line = reader.readLine();
                        if (line != null)
                        {
                            String normalizedLine = normalizePullProgress(line);
                            if (!pushedLines.contains(normalizedLine))
                            {
                            deployLogService.appendLog(logId, line + "\n");
                                pushedLines.add(normalizedLine);
                            }
                        }
                    }
                    break;
                }
                
                // 超时检查
                idleCount++;
                if (idleCount >= maxIdleCount)
                {
                    deployLogService.appendLog(logId, "[ERROR] 镜像拉取超时\n");
                    return false;
                }
                
                Thread.sleep(100);
            }
            
            // 检查退出状态
            int exitStatus = channel.getExitStatus();
            log.info("镜像拉取命令退出码: {}", exitStatus);
            
            return exitStatus == 0;
        }
        finally
        {
            if (channel != null && channel.isConnected())
            {
                channel.disconnect();
            }
        }
    }
    
    /**
     * 标准化拉取进度行，用于去重
     * 将包含百分比的进度行标准化为相同的格式
     */
    private String normalizePullProgress(String line)
    {
        if (line == null)
        {
            return "";
        }
        
        // 移除进度百分比和速度信息，只保留操作类型
        // 例如: "Downloading [==>  ] 12.3MB/45.6MB" -> "Downloading"
        if (line.contains("Downloading") || line.contains("Extracting") || 
            line.contains("Verifying") || line.contains("Pull complete"))
        {
            // 提取层ID和操作类型
            String[] parts = line.split(":");
            if (parts.length >= 2)
            {
                String layerId = parts[0].trim();
                String operation = parts[1].trim().split("\\s+")[0];
                return layerId + ":" + operation;
            }
        }
        
        return line;
    }
    
    /**
     * 更新镜像缓存记录
     */
    @Override
    public void updateImageCache(String imageName, String imageTag, Long serverId, Long imageSize) throws Exception
    {
        if (StringUtils.isEmpty(imageName))
        {
            throw new IllegalArgumentException("镜像名称不能为空");
        }
        
        // 默认标签为 latest
        if (StringUtils.isEmpty(imageTag))
        {
            imageTag = "latest";
        }
        
        // 查询是否已存在
        DockerImage existingImage = dockerImageMapper.selectDockerImageByNameTagServer(imageName, imageTag, serverId);
        
        Date now = new Date();
        
        if (existingImage != null)
        {
            // 更新现有记录
            existingImage.setPullTime(now);
            existingImage.setLastUsedTime(now);
            if (imageSize != null)
            {
                existingImage.setImageSize(String.valueOf(imageSize));
            }
            dockerImageMapper.updateDockerImage(existingImage);
            log.info("更新镜像缓存: {}:{}", imageName, imageTag);
        }
        else
        {
            // 插入新记录
            DockerImage newImage = new DockerImage();
            newImage.setImageName(imageName);
            newImage.setImageTag(imageTag);
            newImage.setServerId(serverId);
            newImage.setImageSize(imageSize != null ? String.valueOf(imageSize) : null);
            newImage.setPullTime(now);
            newImage.setLastUsedTime(now);
            dockerImageMapper.insertDockerImage(newImage);
            log.info("插入镜像缓存: {}:{}", imageName, imageTag);
        }
    }
}
