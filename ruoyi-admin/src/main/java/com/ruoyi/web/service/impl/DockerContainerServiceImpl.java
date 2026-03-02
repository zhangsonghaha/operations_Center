package com.ruoyi.web.service.impl;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import com.ruoyi.web.domain.DockerContainer;
import com.ruoyi.web.domain.OpsServer;
import com.ruoyi.web.domain.dto.DockerContainerStats;
import com.ruoyi.web.mapper.DockerContainerMapper;
import com.ruoyi.web.mapper.OpsServerMapper;
import com.ruoyi.web.service.IDockerContainerService;
import com.ruoyi.web.utils.DockerCommandBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Docker容器Service业务层处理
 * 
 * @author ruoyi
 */
@Service
public class DockerContainerServiceImpl implements IDockerContainerService
{
    private static final Logger log = LoggerFactory.getLogger(DockerContainerServiceImpl.class);
    
    private static final int SSH_CONNECT_TIMEOUT = 30000;
    private static final int COMMAND_TIMEOUT = 5000;
    
    @Autowired
    private DockerContainerMapper dockerContainerMapper;
    
    @Autowired
    private OpsServerMapper opsServerMapper;
    
    /**
     * 查询Docker容器
     */
    @Override
    public DockerContainer selectDockerContainerById(Long containerId)
    {
        return dockerContainerMapper.selectDockerContainerById(containerId);
    }
    
    /**
     * 查询Docker容器列表
     */
    @Override
    public List<DockerContainer> selectDockerContainerList(DockerContainer dockerContainer)
    {
        return dockerContainerMapper.selectDockerContainerList(dockerContainer);
    }
    
    /**
     * 新增Docker容器
     */
    @Override
    public int insertDockerContainer(DockerContainer dockerContainer)
    {
        return dockerContainerMapper.insertDockerContainer(dockerContainer);
    }
    
    /**
     * 修改Docker容器
     */
    @Override
    public int updateDockerContainer(DockerContainer dockerContainer)
    {
        return dockerContainerMapper.updateDockerContainer(dockerContainer);
    }
    
    /**
     * 批量删除Docker容器
     */
    @Override
    public int deleteDockerContainerByIds(Long[] containerIds)
    {
        return dockerContainerMapper.deleteDockerContainerByIds(containerIds);
    }
    
    /**
     * 删除Docker容器信息
     */
    @Override
    public int deleteDockerContainerById(Long containerId)
    {
        return dockerContainerMapper.deleteDockerContainerById(containerId);
    }
    
    /**
     * 获取容器日志
     */
    @Override
    public String getContainerLogs(Long containerId, Integer tail) throws Exception
    {
        DockerContainer container = dockerContainerMapper.selectDockerContainerById(containerId);
        if (container == null)
        {
            throw new Exception("容器不存在");
        }
        
        OpsServer server = opsServerMapper.selectOpsServerByServerId(container.getServerId());
        if (server == null)
        {
            throw new Exception("服务器不存在");
        }
        
        Session session = null;
        try
        {
            // 建立SSH连接
            JSch jsch = new JSch();
            session = jsch.getSession(server.getUsername(), server.getPublicIp(), server.getServerPort());
            session.setPassword(server.getPassword());
            session.setConfig("StrictHostKeyChecking", "no");
            session.connect(SSH_CONNECT_TIMEOUT);
            
            // 构建日志命令
            String command = DockerCommandBuilder.buildDockerLogsCommand(container.getContainerName(), tail);
            
            // 执行命令
            String logs = executeCommand(session, command);
            
            return logs;
        }
        finally
        {
            if (session != null && session.isConnected())
            {
                session.disconnect();
            }
        }
    }
    
    /**
     * 获取容器统计信息
     */
    @Override
    public DockerContainerStats getContainerStats(Long containerId) throws Exception
    {
        DockerContainer container = dockerContainerMapper.selectDockerContainerById(containerId);
        if (container == null)
        {
            throw new Exception("容器不存在");
        }
        
        OpsServer server = opsServerMapper.selectOpsServerByServerId(container.getServerId());
        if (server == null)
        {
            throw new Exception("服务器不存在");
        }
        
        Session session = null;
        try
        {
            // 建立SSH连接
            JSch jsch = new JSch();
            session = jsch.getSession(server.getUsername(), server.getPublicIp(), server.getServerPort());
            session.setPassword(server.getPassword());
            session.setConfig("StrictHostKeyChecking", "no");
            session.connect(SSH_CONNECT_TIMEOUT);
            
            // 执行 docker stats 命令
            String command = "docker stats --no-stream --format '{{.CPUPerc}}|{{.MemUsage}}|{{.NetIO}}|{{.BlockIO}}' " + container.getContainerName();
            String output = executeCommand(session, command);
            
            // 解析输出
            DockerContainerStats stats = new DockerContainerStats();
            stats.setContainerId(container.getContainerDockerId()); // 使用 Docker 容器ID（字符串）
            stats.setContainerName(container.getContainerName());
            
            if (output != null && !output.trim().isEmpty())
            {
                String[] parts = output.trim().split("\\|");
                if (parts.length >= 4)
                {
                    // 解析 CPU 百分比（例如: "0.50%"）
                    String cpuStr = parts[0].trim().replace("%", "");
                    try {
                        stats.setCpuPercent(new java.math.BigDecimal(cpuStr));
                    } catch (Exception e) {
                        log.warn("解析CPU百分比失败: {}", cpuStr);
                    }
                    
                    // 内存使用量和网络IO暂时保存为字符串格式
                    // 例如: "1.5GiB / 2GiB", "1.2kB / 3.4kB"
                    // 这里简化处理，实际使用时可以进一步解析
                }
            }
            
            return stats;
        }
        finally
        {
            if (session != null && session.isConnected())
            {
                session.disconnect();
            }
        }
    }
    
    /**
     * 执行SSH命令
     */
    private String executeCommand(Session session, String command) throws Exception
    {
        ChannelExec channel = null;
        try
        {
            channel = (ChannelExec) session.openChannel("exec");
            channel.setCommand(command);
            
            InputStream in = channel.getInputStream();
            InputStream err = channel.getErrStream();
            
            channel.connect(COMMAND_TIMEOUT);
            
            // 读取输出
            BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
            BufferedReader errReader = new BufferedReader(new InputStreamReader(err, StandardCharsets.UTF_8));
            
            StringBuilder output = new StringBuilder();
            StringBuilder errorOutput = new StringBuilder();
            String line;
            
            // 等待命令执行完成
            int timeout = 0;
            while (!channel.isClosed() && timeout < COMMAND_TIMEOUT)
            {
                while (reader.ready())
                {
                    line = reader.readLine();
                    if (line != null)
                    {
                        output.append(line).append("\n");
                    }
                }
                
                while (errReader.ready())
                {
                    line = errReader.readLine();
                    if (line != null)
                    {
                        errorOutput.append(line).append("\n");
                    }
                }
                
                Thread.sleep(100);
                timeout += 100;
            }
            
            // 最后再读取一次
            while (reader.ready())
            {
                line = reader.readLine();
                if (line != null)
                {
                    output.append(line).append("\n");
                }
            }
            
            int exitStatus = channel.getExitStatus();
            
            if (exitStatus != 0 && errorOutput.length() > 0)
            {
                throw new Exception(errorOutput.toString());
            }
            
            return output.toString();
        }
        finally
        {
            if (channel != null && channel.isConnected())
            {
                channel.disconnect();
            }
        }
    }
}
