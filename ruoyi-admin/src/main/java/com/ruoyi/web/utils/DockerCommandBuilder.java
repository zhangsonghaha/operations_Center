package com.ruoyi.web.utils;

import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.web.domain.dto.DockerDeployConfig;
import com.ruoyi.web.domain.dto.PortMapping;
import com.ruoyi.web.domain.dto.VolumeMount;

import java.math.BigDecimal;
import java.util.Map;

/**
 * Docker命令构建工具类
 * 
 * @author ruoyi
 */
public class DockerCommandBuilder
{
    /**
     * 默认CPU限制（核数）
     */
    private static final BigDecimal DEFAULT_CPU_LIMIT = new BigDecimal("2.00");

    /**
     * 默认内存限制
     */
    private static final String DEFAULT_MEMORY_LIMIT = "2g";

    /**
     * 最大CPU限制（核数）
     */
    private static final BigDecimal MAX_CPU_LIMIT = new BigDecimal("4.00");

    /**
     * 最大内存限制（字节）
     */
    private static final long MAX_MEMORY_BYTES = 4L * 1024 * 1024 * 1024; // 4GB

    /**
     * 构建Docker run命令
     * 
     * @param config 部署配置
     * @return Docker run命令
     */
    public static String buildDockerRunCommand(DockerDeployConfig config)
    {
        if (config == null)
        {
            throw new IllegalArgumentException("部署配置不能为空");
        }

        if (StringUtils.isEmpty(config.getContainerName()))
        {
            throw new IllegalArgumentException("容器名称不能为空");
        }

        if (StringUtils.isEmpty(config.getImageName()))
        {
            throw new IllegalArgumentException("镜像名称不能为空");
        }

        StringBuilder command = new StringBuilder();

        // 1. 基础命令
        command.append("docker run -d");

        // 2. 容器名称
        command.append(" --name ").append(escapeShellArg(config.getContainerName()));

        // 3. 端口映射
        if (config.getPorts() != null && !config.getPorts().isEmpty())
        {
            for (PortMapping port : config.getPorts())
            {
                command.append(" -p ");
                command.append(port.getHostPort());
                command.append(":");
                command.append(port.getContainerPort());
                
                if (StringUtils.isNotEmpty(port.getProtocol()))
                {
                    command.append("/").append(port.getProtocol());
                }
            }
        }

        // 4. 环境变量
        if (config.getEnvVars() != null && !config.getEnvVars().isEmpty())
        {
            for (Map.Entry<String, String> entry : config.getEnvVars().entrySet())
            {
                command.append(" -e ");
                command.append(escapeShellArg(entry.getKey()));
                command.append("=");
                command.append(escapeShellArg(entry.getValue()));
            }
        }

        // 5. 卷挂载
        if (config.getVolumes() != null && !config.getVolumes().isEmpty())
        {
            for (VolumeMount volume : config.getVolumes())
            {
                command.append(" -v ");
                command.append(escapeShellArg(volume.getHostPath()));
                command.append(":");
                command.append(volume.getContainerPath());
                
                if (StringUtils.isNotEmpty(volume.getMode()))
                {
                    command.append(":").append(volume.getMode());
                }
            }
        }

        // 6. 资源限制
        BigDecimal cpuLimit = config.getCpuLimit();
        String memoryLimit = config.getMemoryLimit();

        // 应用默认值和最大限制
        if (cpuLimit == null)
        {
            cpuLimit = DEFAULT_CPU_LIMIT;
        }
        else if (cpuLimit.compareTo(MAX_CPU_LIMIT) > 0)
        {
            cpuLimit = MAX_CPU_LIMIT;
        }

        if (StringUtils.isEmpty(memoryLimit))
        {
            memoryLimit = DEFAULT_MEMORY_LIMIT;
        }
        else
        {
            // 验证内存限制不超过最大值
            long memoryBytes = parseMemoryLimit(memoryLimit);
            if (memoryBytes > MAX_MEMORY_BYTES)
            {
                memoryLimit = "4g";
            }
        }

        command.append(" --cpus ").append(cpuLimit);
        command.append(" --memory ").append(memoryLimit);

        // 7. 重启策略
        String restartPolicy = config.getRestartPolicy();
        if (StringUtils.isEmpty(restartPolicy))
        {
            restartPolicy = "unless-stopped";
        }
        command.append(" --restart ").append(restartPolicy);

        // 8. 网络模式
        if (StringUtils.isNotEmpty(config.getNetworkMode()))
        {
            command.append(" --network ").append(config.getNetworkMode());
        }

        // 9. 镜像名称和标签
        command.append(" ");
        command.append(config.getImageName());
        if (StringUtils.isNotEmpty(config.getImageTag()))
        {
            command.append(":").append(config.getImageTag());
        }
        else
        {
            command.append(":latest");
        }

        return command.toString();
    }

    /**
     * 构建Docker stop命令
     * 
     * @param containerName 容器名称
     * @return Docker stop命令
     */
    public static String buildDockerStopCommand(String containerName)
    {
        if (StringUtils.isEmpty(containerName))
        {
            throw new IllegalArgumentException("容器名称不能为空");
        }

        return "docker stop " + escapeShellArg(containerName);
    }

    /**
     * 构建Docker start命令
     * 
     * @param containerName 容器名称
     * @return Docker start命令
     */
    public static String buildDockerStartCommand(String containerName)
    {
        if (StringUtils.isEmpty(containerName))
        {
            throw new IllegalArgumentException("容器名称不能为空");
        }

        return "docker start " + escapeShellArg(containerName);
    }

    /**
     * 构建Docker restart命令
     * 
     * @param containerName 容器名称
     * @return Docker restart命令
     */
    public static String buildDockerRestartCommand(String containerName)
    {
        if (StringUtils.isEmpty(containerName))
        {
            throw new IllegalArgumentException("容器名称不能为空");
        }

        return "docker restart " + escapeShellArg(containerName);
    }

    /**
     * 构建Docker rm命令
     * 
     * @param containerName 容器名称
     * @param force 是否强制删除
     * @return Docker rm命令
     */
    public static String buildDockerRmCommand(String containerName, boolean force)
    {
        if (StringUtils.isEmpty(containerName))
        {
            throw new IllegalArgumentException("容器名称不能为空");
        }

        StringBuilder command = new StringBuilder("docker rm");
        if (force)
        {
            command.append(" -f");
        }
        command.append(" ").append(escapeShellArg(containerName));

        return command.toString();
    }

    /**
     * 构建Docker logs命令
     * 
     * @param containerName 容器名称
     * @param tail 显示最后N行（null表示全部）
     * @return Docker logs命令
     */
    public static String buildDockerLogsCommand(String containerName, Integer tail)
    {
        if (StringUtils.isEmpty(containerName))
        {
            throw new IllegalArgumentException("容器名称不能为空");
        }

        StringBuilder command = new StringBuilder("docker logs");
        if (tail != null && tail > 0)
        {
            command.append(" --tail ").append(tail);
        }
        command.append(" ").append(escapeShellArg(containerName));

        return command.toString();
    }

    /**
     * 构建Docker inspect命令
     * 
     * @param containerName 容器名称
     * @return Docker inspect命令
     */
    public static String buildDockerInspectCommand(String containerName)
    {
        if (StringUtils.isEmpty(containerName))
        {
            throw new IllegalArgumentException("容器名称不能为空");
        }

        return "docker inspect " + escapeShellArg(containerName);
    }

    /**
     * Shell参数转义
     * 移除危险字符并用单引号包裹参数值
     * 
     * @param arg 参数值
     * @return 转义后的参数
     */
    public static String escapeShellArg(String arg)
    {
        if (arg == null)
        {
            return "''";
        }

        // 移除危险字符
        String cleaned = arg.replaceAll("[;&|`$(){}\\[\\]<>]", "");

        // 转义单引号：将 ' 替换为 '\''
        cleaned = cleaned.replace("'", "'\\''");

        // 用单引号包裹
        return "'" + cleaned + "'";
    }

    /**
     * 解析内存限制字符串为字节数
     * 
     * @param memoryLimit 内存限制（如: 2g, 512m, 1024k）
     * @return 字节数
     */
    private static long parseMemoryLimit(String memoryLimit)
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
                return Long.parseLong(memoryLimit);
            }
        }
        catch (NumberFormatException e)
        {
            return 0;
        }
    }
}
