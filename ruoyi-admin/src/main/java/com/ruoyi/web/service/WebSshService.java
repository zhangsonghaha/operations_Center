package com.ruoyi.web.service;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.ruoyi.web.domain.OpsServer;
import com.ruoyi.web.mapper.OpsServerMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelShell;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.web.socket.WebSshData;

/**
 * WebSSH业务逻辑处理
 */
@Service
public class WebSshService
{
    private static final Logger log = LoggerFactory.getLogger(WebSshService.class);

    private static ExecutorService executorService = Executors.newCachedThreadPool();
    private static Map<String, Object> sshMap = new ConcurrentHashMap<>();

    @Autowired
    private OpsServerMapper opsServerMapper;

    /**
     * 初始化连接
     */
    public void initConnection(WebSocketSession session)
    {
        JSch jSch = new JSch();
        sshMap.put(session.getId(), jSch);
    }

    /**
     * 处理客户端发送的数据
     */
    public void recvHandle(String buffer, WebSocketSession session)
    {
        ObjectMapper objectMapper = new ObjectMapper();
        WebSshData webSshData = null;
        try
        {
            webSshData = objectMapper.readValue(buffer, WebSshData.class);
        }
        catch (IOException e)
        {
            log.error("Json转换异常", e);
            return;
        }

        if ("connect".equals(webSshData.getType()))
        {
            // 找到服务器信息
            OpsServer server = opsServerMapper.selectOpsServerByServerId(webSshData.getServerId());
            if (server == null) {
                sendMessage(session, "服务器不存在\r\n".getBytes());
                close(session);
                return;
            }
            
            // 启动线程处理连接
            final WebSshData finalWebSshData = webSshData;
            executorService.execute(new Runnable()
            {
                @Override
                public void run()
                {
                    try
                    {
                        connectToSsh(session, server, finalWebSshData);
                    }
                    catch (Exception e)
                    {
                        log.error("WebSSH连接异常", e);
                        sendMessage(session, ("连接异常: " + e.getMessage() + "\r\n").getBytes());
                        close(session);
                    }
                }
            });
        }
        else if ("command".equals(webSshData.getType()))
        {
            String command = webSshData.getCommand();
            if (command != null)
            {
                try
                {
                    transToSsh(session, command);
                }
                catch (IOException e)
                {
                    log.error("WebSSH数据传输异常", e);
                    close(session);
                }
            }
        }
        else if ("resize".equals(webSshData.getType()))
        {
            Object obj = sshMap.get(session.getId());
            if (obj instanceof ChannelShell) {
                ChannelShell channel = (ChannelShell) obj;
                channel.setPtySize(webSshData.getCols(), webSshData.getRows(), 640, 480);
            }
        }
    }

    public void sendMessage(WebSocketSession session, byte[] buffer)
    {
        try
        {
            session.sendMessage(new TextMessage(buffer));
        }
        catch (IOException e)
        {
            log.error("WebSSH发送消息异常", e);
        }
    }

    public void close(WebSocketSession session)
    {
        try
        {
            // 关闭Channel
            Object obj = sshMap.get(session.getId());
            if (obj instanceof ChannelShell)
            {
                ((ChannelShell) obj).disconnect();
            }
            // 关闭Session
            // 注意：这里需要维护Session和Channel的对应关系，简化起见假设是一对一
            // 实际可能需要更复杂的Map结构
            sshMap.remove(session.getId());
            session.close();
        }
        catch (IOException e)
        {
            log.error("WebSSH关闭异常", e);
        }
    }

    private void connectToSsh(WebSocketSession session, OpsServer server, WebSshData webSshData) throws Exception
    {
        JSch jSch = new JSch();
        if ("1".equals(server.getAuthType()) && StringUtils.isNotEmpty(server.getPrivateKey())) {
            jSch.addIdentity("key", server.getPrivateKey().getBytes(), null, null);
        }

        String host = StringUtils.isNotEmpty(server.getPrivateIp()) ? server.getPrivateIp() : server.getPublicIp();
        int port = server.getServerPort() != null ? server.getServerPort() : 22;

        Session sshSession = jSch.getSession(server.getUsername(), host, port);
        if ("0".equals(server.getAuthType())) {
            sshSession.setPassword(server.getPassword());
        }
        
        Properties config = new Properties();
        config.put("StrictHostKeyChecking", "no");
        sshSession.setConfig(config);
        
        sshSession.connect(30000);

        Channel channel = sshSession.openChannel("shell");
        ChannelShell channelShell = (ChannelShell) channel;
        channelShell.setPtySize(webSshData.getCols(), webSshData.getRows(), 640, 480);
        
        channel.connect(3000);
        
        sshMap.put(session.getId(), channel);

        // 读取SSH输出
        InputStream inputStream = channel.getInputStream();
        try
        {
            byte[] buffer = new byte[1024];
            int i = 0;
            while ((i = inputStream.read(buffer)) != -1)
            {
                sendMessage(session, java.util.Arrays.copyOfRange(buffer, 0, i));
            }
        }
        finally
        {
            sshSession.disconnect();
            channel.disconnect();
            if (inputStream != null)
            {
                inputStream.close();
            }
        }
    }

    private void transToSsh(WebSocketSession session, String command) throws IOException
    {
        Object obj = sshMap.get(session.getId());
        if (obj instanceof ChannelShell)
        {
            ChannelShell channel = (ChannelShell) obj;
            OutputStream outputStream = channel.getOutputStream();
            outputStream.write(command.getBytes());
            outputStream.flush();
        }
    }
}
