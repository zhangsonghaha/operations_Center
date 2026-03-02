package com.ruoyi.web.service.impl;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.Session;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.web.domain.dto.VolumeMount;
import com.ruoyi.web.service.IDockerValidationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Docker部署前验证Service业务层处理
 * 
 * @author ruoyi
 */
@Service
public class DockerValidationServiceImpl implements IDockerValidationService
{
    private static final Logger log = LoggerFactory.getLogger(DockerValidationServiceImpl.class);

    /**
     * 容器名称正则：只允许字母、数字、下划线、连字符
     */
    private static final Pattern CONTAINER_NAME_PATTERN = Pattern.compile("^[a-zA-Z0-9_-]+$");

    /**
     * 敏感路径列表
     */
    private static final List<String> SENSITIVE_PATHS = Arrays.asList(
        "/etc", "/root", "/var/run/docker.sock", "/sys", "/proc", "/boot", "/dev"
    );

    /**
     * 最大CPU限制（核数）
     */
    private static final BigDecimal MAX_CPU_LIMIT = new BigDecimal("4.00");

    /**
     * 最大内存限制（字节）
     */
    private static final long MAX_MEMORY_BYTES = 4L * 1024 * 1024 * 1024; // 4GB

    /**
     * 验证容器名称格式
     */
    @Override
    public void validateContainerName(String containerName)
    {
        if (StringUtils.isEmpty(containerName))
        {
            throw new IllegalArgumentException("容器名称不能为空");
        }

        if (containerName.length() < 2 || containerName.length() > 100)
        {
            throw new IllegalArgumentException("容器名称长度必须在2-100个字符之间");
        }

        if (!CONTAINER_NAME_PATTERN.matcher(containerName).matches())
        {
            throw new IllegalArgumentException("容器名称只能包含字母、数字、下划线和连字符");
        }
    }

    /**
     * 检查容器名称是否已存在
     */
    @Override
    public boolean checkContainerNameExists(Session session, String containerName)
    {
        try
        {
            // 使用 docker ps -a 查询所有容器（包括已停止的）
            String command = String.format("docker ps -a --filter name=^%s$ --format '{{.Names}}'", containerName);
            String output = executeCommand(session, command);

            if (output == null || output.trim().isEmpty())
            {
                return false;
            }

            // 检查是否完全匹配容器名称
            String[] lines = output.trim().split("\n");
            for (String line : lines)
            {
                if (containerName.equals(line.trim()))
                {
                    return true;
                }
            }

            return false;
        }
        catch (Exception e)
        {
            log.error("检查容器名称失败: {}", containerName, e);
            // 出错时保守处理，假设已存在
            return true;
        }
    }

    /**
     * 检查端口是否被占用
     */
    @Override
    public boolean checkPortInUse(Session session, int port)
    {
        if (port < 1 || port > 65535)
        {
            throw new IllegalArgumentException("端口号必须在1-65535之间");
        }

        try
        {
            // 优先使用 ss 命令（更现代）
            String command = String.format("ss -tuln | grep ':%d '", port);
            String output = executeCommand(session, command);

            if (output != null && !output.trim().isEmpty())
            {
                return true;
            }

            // 回退到 netstat 命令
            command = String.format("netstat -tuln 2>/dev/null | grep ':%d '", port);
            output = executeCommand(session, command);

            if (output != null && !output.trim().isEmpty())
            {
                return true;
            }

            // 最后尝试 lsof 命令
            command = String.format("lsof -i :%d 2>/dev/null", port);
            output = executeCommand(session, command);

            return output != null && !output.trim().isEmpty();
        }
        catch (Exception e)
        {
            log.error("检查端口占用失败: {}", port, e);
            // 出错时保守处理，假设端口被占用
            return true;
        }
    }

    /**
     * 验证卷挂载路径安全性
     */
    @Override
    public void validateVolumeMounts(List<VolumeMount> volumes)
    {
        if (volumes == null || volumes.isEmpty())
        {
            return;
        }

        for (VolumeMount volume : volumes)
        {
            String hostPath = volume.getHostPath();
            if (StringUtils.isEmpty(hostPath))
            {
                throw new IllegalArgumentException("卷挂载的主机路径不能为空");
            }

            // 检查是否包含敏感路径
            for (String sensitivePath : SENSITIVE_PATHS)
            {
                if (hostPath.equals(sensitivePath) || hostPath.startsWith(sensitivePath + "/"))
                {
                    throw new SecurityException(
                        String.format("禁止挂载敏感路径: %s (包含 %s)", hostPath, sensitivePath)
                    );
                }
            }

            // 检查容器路径
            String containerPath = volume.getContainerPath();
            if (StringUtils.isEmpty(containerPath))
            {
                throw new IllegalArgumentException("卷挂载的容器路径不能为空");
            }

            // 验证挂载模式
            String mode = volume.getMode();
            if (StringUtils.isNotEmpty(mode) && !mode.equals("rw") && !mode.equals("ro"))
            {
                throw new IllegalArgumentException("卷挂载模式只能是 rw（读写）或 ro（只读）");
            }
        }
    }

    /**
     * 验证资源限制
     */
    @Override
    public void validateResourceLimits(BigDecimal cpuLimit, String memoryLimit)
    {
        // 验证CPU限制
        if (cpuLimit != null)
        {
            if (cpuLimit.compareTo(BigDecimal.ZERO) <= 0)
            {
                throw new IllegalArgumentException("CPU限制必须大于0");
            }

            if (cpuLimit.compareTo(MAX_CPU_LIMIT) > 0)
            {
                throw new IllegalArgumentException(
                    String.format("CPU限制不能超过 %s 核", MAX_CPU_LIMIT)
                );
            }
        }

        // 验证内存限制
        if (StringUtils.isNotEmpty(memoryLimit))
        {
            long memoryBytes = parseMemoryLimit(memoryLimit);
            
            if (memoryBytes <= 0)
            {
                throw new IllegalArgumentException("内存限制必须大于0");
            }

            if (memoryBytes > MAX_MEMORY_BYTES)
            {
                throw new IllegalArgumentException(
                    String.format("内存限制不能超过 %dGB", MAX_MEMORY_BYTES / 1024 / 1024 / 1024)
                );
            }

            // 最小内存限制：128MB
            if (memoryBytes < 128 * 1024 * 1024)
            {
                throw new IllegalArgumentException("内存限制不能小于128MB");
            }
        }
    }

    /**
     * 解析内存限制字符串为字节数
     * 
     * @param memoryLimit 内存限制（如: 2g, 512m, 1024k）
     * @return 字节数
     */
    private long parseMemoryLimit(String memoryLimit)
    {
        if (StringUtils.isEmpty(memoryLimit))
        {
            return 0;
        }

        memoryLimit = memoryLimit.trim().toLowerCase();
        
        try
        {
            if (memoryLimit.endsWith("g"))
            {
                String value = memoryLimit.substring(0, memoryLimit.length() - 1);
                return (long) (Double.parseDouble(value) * 1024 * 1024 * 1024);
            }
            else if (memoryLimit.endsWith("m"))
            {
                String value = memoryLimit.substring(0, memoryLimit.length() - 1);
                return (long) (Double.parseDouble(value) * 1024 * 1024);
            }
            else if (memoryLimit.endsWith("k"))
            {
                String value = memoryLimit.substring(0, memoryLimit.length() - 1);
                return (long) (Double.parseDouble(value) * 1024);
            }
            else
            {
                // 假设是字节数
                return Long.parseLong(memoryLimit);
            }
        }
        catch (NumberFormatException e)
        {
            throw new IllegalArgumentException("内存限制格式不正确: " + memoryLimit);
        }
    }

    /**
     * 执行SSH命令并返回输出
     */
    private String executeCommand(Session session, String command)
    {
        ChannelExec channel = null;
        try
        {
            channel = (ChannelExec) session.openChannel("exec");
            channel.setCommand(command);
            channel.setErrStream(System.err);

            InputStream in = channel.getInputStream();
            channel.connect(5000);

            StringBuilder output = new StringBuilder();
            BufferedReader reader = new BufferedReader(
                new InputStreamReader(in, StandardCharsets.UTF_8));

            String line;
            while ((line = reader.readLine()) != null)
            {
                output.append(line).append("\n");
            }

            // 等待命令执行完成
            int timeout = 10; // 10秒超时
            while (!channel.isClosed() && timeout > 0)
            {
                try
                {
                    Thread.sleep(100);
                    timeout--;
                }
                catch (InterruptedException e)
                {
                    Thread.currentThread().interrupt();
                    break;
                }
            }

            int exitStatus = channel.getExitStatus();
            if (exitStatus != 0 && exitStatus != -1)
            {
                log.debug("命令执行失败，退出码: {}, 命令: {}", exitStatus, command);
                return null;
            }

            return output.toString();
        }
        catch (Exception e)
        {
            log.error("执行命令失败: {}", command, e);
            return null;
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
