package com.ruoyi.web.service.impl;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import com.ruoyi.web.domain.OpsServer;
import com.ruoyi.web.domain.dto.DockerEnvInfo;
import com.ruoyi.web.mapper.OpsServerMapper;
import com.ruoyi.web.service.IDockerEnvService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

/**
 * Docker环境检测Service业务层处理
 * 
 * @author ruoyi
 */
@Service
public class DockerEnvServiceImpl implements IDockerEnvService
{
    private static final Logger log = LoggerFactory.getLogger(DockerEnvServiceImpl.class);

    @Autowired
    private OpsServerMapper opsServerMapper;

    /**
     * 检查Docker环境
     */
    @Override
    public DockerEnvInfo checkDockerEnvironment(Long serverId)
    {
        Session session = null;
        try
        {
            // 获取服务器信息
            OpsServer server = opsServerMapper.selectOpsServerByServerId(serverId);
            if (server == null)
            {
                DockerEnvInfo info = new DockerEnvInfo();
                info.setDockerInstalled(false);
                info.setErrorMessage("服务器信息不存在");
                return info;
            }

            // 建立SSH连接
            JSch jsch = new JSch();
            session = jsch.getSession(server.getUsername(), server.getPublicIp(), server.getServerPort());
            session.setPassword(server.getPassword());
            session.setConfig("StrictHostKeyChecking", "no");
            session.connect(30000);

            // 检查Docker环境
            return checkDockerEnvironment(session);
        }
        catch (Exception e)
        {
            log.error("检查Docker环境失败", e);
            DockerEnvInfo info = new DockerEnvInfo();
            info.setDockerInstalled(false);
            info.setErrorMessage("连接服务器失败: " + e.getMessage());
            return info;
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
     * 检查Docker环境（使用已有Session）
     */
    @Override
    public DockerEnvInfo checkDockerEnvironment(Session session)
    {
        DockerEnvInfo info = new DockerEnvInfo();

        try
        {
            // 1. 检查Docker是否安装
            String versionOutput = executeCommand(session, "docker --version");
            if (versionOutput == null || versionOutput.isEmpty())
            {
                info.setDockerInstalled(false);
                info.setDockerRunning(false);
                info.setErrorMessage("Docker未安装");
                return info;
            }

            info.setDockerInstalled(true);

            // 解析Docker版本
            // 输出格式: Docker version 20.10.7, build f0df350
            if (versionOutput.contains("Docker version"))
            {
                String version = versionOutput.substring(versionOutput.indexOf("version") + 8);
                if (version.contains(","))
                {
                    version = version.substring(0, version.indexOf(",")).trim();
                }
                info.setDockerVersion(version);
            }
            else
            {
                info.setDockerVersion(versionOutput.trim());
            }

            // 2. 检查Docker服务是否运行
            String infoOutput = executeCommand(session, "docker info --format '{{json .}}'");
            if (infoOutput == null || infoOutput.contains("Cannot connect to the Docker daemon"))
            {
                info.setDockerRunning(false);
                info.setErrorMessage("Docker服务未运行");
                return info;
            }

            info.setDockerRunning(true);

            // 3. 解析Docker信息
            try
            {
                // 获取存储驱动
                String driverOutput = executeCommand(session, "docker info --format '{{.Driver}}'");
                if (driverOutput != null && !driverOutput.isEmpty())
                {
                    info.setStorageDriver(driverOutput.trim());
                }

                // 获取Docker根目录
                String rootDirOutput = executeCommand(session, "docker info --format '{{.DockerRootDir}}'");
                if (rootDirOutput != null && !rootDirOutput.isEmpty())
                {
                    info.setDockerRootDir(rootDirOutput.trim());

                    // 4. 获取磁盘空间信息
                    String spaceOutput = executeCommand(session, 
                        "df -B1 " + info.getDockerRootDir() + " | tail -1 | awk '{print $2,$3}'");
                    if (spaceOutput != null && !spaceOutput.isEmpty())
                    {
                        String[] parts = spaceOutput.trim().split("\\s+");
                        if (parts.length >= 2)
                        {
                            try
                            {
                                info.setTotalSpace(Long.parseLong(parts[0]));
                                info.setUsedSpace(Long.parseLong(parts[1]));
                            }
                            catch (NumberFormatException e)
                            {
                                log.warn("解析磁盘空间失败: {}", spaceOutput);
                            }
                        }
                    }
                }
            }
            catch (Exception e)
            {
                log.warn("解析Docker详细信息失败", e);
                // 不影响主要检测结果
            }

            return info;
        }
        catch (Exception e)
        {
            log.error("检查Docker环境失败", e);
            info.setDockerInstalled(false);
            info.setDockerRunning(false);
            info.setErrorMessage("检查失败: " + e.getMessage());
            return info;
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
