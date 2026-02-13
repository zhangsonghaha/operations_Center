package com.ruoyi.web.service.impl;

import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.List;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.web.mapper.OpsServerMapper;
import com.ruoyi.web.domain.OpsServer;
import com.ruoyi.web.service.IOpsServerService;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;

/**
 * 服务器资产Service业务层处理
 */
@Service
public class OpsServerServiceImpl implements IOpsServerService 
{
    @Autowired
    private OpsServerMapper opsServerMapper;

    /**
     * 检测服务器连接状态（内网/公网 + SSH验证）
     */
    @Override
    public String checkConnection(Long serverId)
    {
        OpsServer server = opsServerMapper.selectOpsServerByServerId(serverId);
        if (server == null)
        {
            return "服务器不存在";
        }
        
        StringBuilder result = new StringBuilder();
        int port = server.getServerPort() != null ? server.getServerPort() : 22;
        boolean intranetConnected = false;

        // 1. 尝试内网连接
        if (StringUtils.isNotEmpty(server.getPrivateIp())) {
            if (isPortOpen(server.getPrivateIp(), port)) {
                intranetConnected = true;
                result.append("内网端口通");
                String sshResult = checkSshLogin(server.getPrivateIp(), port, server);
                if ("Success".equals(sshResult)) {
                    return "🟢 内网可达 (SSH验证通过)";
                } else {
                    result.append(" (SSH: ").append(sshResult).append(")");
                }
            } else {
                result.append("内网不可达");
            }
        }

        // 2. 尝试公网连接（如果内网不通或未配置）
        if (!intranetConnected && StringUtils.isNotEmpty(server.getPublicIp())) {
            if (result.length() > 0) result.append(" | ");
            
            if (isPortOpen(server.getPublicIp(), port)) {
                result.append("公网端口通");
                String sshResult = checkSshLogin(server.getPublicIp(), port, server);
                if ("Success".equals(sshResult)) {
                    return "🔵 公网可达 (SSH验证通过)";
                } else {
                    result.append(" (SSH: ").append(sshResult).append(")");
                }
            } else {
                result.append("公网不可达");
            }
        }

        return result.toString().isEmpty() ? "🔴 未配置IP" : "🟡 " + result.toString();
    }

    private boolean isPortOpen(String ip, int port) {
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(ip, port), 2000); // 2秒超时
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private String checkSshLogin(String ip, int port, OpsServer server) {
        Session session = null;
        try {
            JSch jsch = new JSch();
            if ("1".equals(server.getAuthType()) && StringUtils.isNotEmpty(server.getPrivateKey())) {
                // 密钥认证
                String privateKey = server.getPrivateKey();
                // JSch 需要密钥的 byte[]
                jsch.addIdentity("key", privateKey.getBytes(), null, null);
            }

            session = jsch.getSession(server.getUsername(), ip, port);
            
            if ("0".equals(server.getAuthType())) {
                // 密码认证
                session.setPassword(server.getPassword());
            }

            // 跳过 HostKey 检查
            session.setConfig("StrictHostKeyChecking", "no");
            session.setConfig("PreferredAuthentications", "publickey,password");
            
            // 尝试连接，超时 5秒
            session.connect(5000);
            
            return "Success";
        } catch (Exception e) {
            return "认证失败";
        } finally {
            if (session != null && session.isConnected()) {
                session.disconnect();
            }
        }
    }

    /**
     * 查询服务器资产
     */
    @Override
    public OpsServer selectOpsServerByServerId(Long serverId)
    {
        return opsServerMapper.selectOpsServerByServerId(serverId);
    }

    /**
     * 查询服务器资产列表
     */
    @Override
    public List<OpsServer> selectOpsServerList(OpsServer opsServer)
    {
        return opsServerMapper.selectOpsServerList(opsServer);
    }

    /**
     * 新增服务器资产
     */
    @Override
    public int insertOpsServer(OpsServer opsServer)
    {
        opsServer.setCreateTime(DateUtils.getNowDate());
        return opsServerMapper.insertOpsServer(opsServer);
    }

    /**
     * 修改服务器资产
     */
    @Override
    public int updateOpsServer(OpsServer opsServer)
    {
        opsServer.setUpdateTime(DateUtils.getNowDate());
        return opsServerMapper.updateOpsServer(opsServer);
    }

    /**
     * 批量删除服务器资产
     */
    @Override
    public int deleteOpsServerByServerIds(Long[] serverIds)
    {
        return opsServerMapper.deleteOpsServerByServerIds(serverIds);
    }

    /**
     * 删除服务器资产信息
     */
    @Override
    public int deleteOpsServerByServerId(Long serverId)
    {
        return opsServerMapper.deleteOpsServerByServerId(serverId);
    }
}
