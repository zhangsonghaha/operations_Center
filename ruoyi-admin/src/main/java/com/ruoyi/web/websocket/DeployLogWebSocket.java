package com.ruoyi.web.websocket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * 部署日志WebSocket
 * 
 * @author ruoyi
 */
@ServerEndpoint("/websocket/deployLog/{logId}")
@Component
public class DeployLogWebSocket {
    
    private static final Logger log = LoggerFactory.getLogger(DeployLogWebSocket.class);
    
    /** 存储每个日志ID对应的会话集合 */
    private static ConcurrentHashMap<String, CopyOnWriteArraySet<DeployLogWebSocket>> logSessions = new ConcurrentHashMap<>();
    
    /** 当前会话 */
    private Session session;
    
    /** 日志ID */
    private String logId;
    
    /**
     * 连接建立成功调用的方法
     */
    @OnOpen
    public void onOpen(Session session, @PathParam("logId") String logId) {
        this.session = session;
        this.logId = logId;
        
        // 添加到对应日志ID的会话集合
        CopyOnWriteArraySet<DeployLogWebSocket> sessions = logSessions.get(logId);
        if (sessions == null) {
            sessions = new CopyOnWriteArraySet<>();
            logSessions.put(logId, sessions);
        }
        sessions.add(this);
        
        log.info("WebSocket连接建立: logId={}, 当前连接数={}", logId, sessions.size());
    }
    
    /**
     * 连接关闭调用的方法
     */
    @OnClose
    public void onClose() {
        CopyOnWriteArraySet<DeployLogWebSocket> sessions = logSessions.get(logId);
        if (sessions != null) {
            sessions.remove(this);
            if (sessions.isEmpty()) {
                logSessions.remove(logId);
            }
        }
        log.info("WebSocket连接关闭: logId={}", logId);
    }
    
    /**
     * 收到客户端消息后调用的方法
     */
    @OnMessage
    public void onMessage(String message, Session session) {
        log.debug("收到客户端消息: logId={}, message={}", logId, message);
    }
    
    /**
     * 发生错误时调用
     */
    @OnError
    public void onError(Session session, Throwable error) {
        log.error("WebSocket发生错误: logId={}", logId, error);
    }
    
    /**
     * 向指定日志ID的所有客户端推送消息
     */
    public static void sendMessage(String logId, String message) {
        CopyOnWriteArraySet<DeployLogWebSocket> sessions = logSessions.get(logId);
        if (sessions != null) {
            for (DeployLogWebSocket webSocket : sessions) {
                try {
                    webSocket.session.getBasicRemote().sendText(message);
                } catch (IOException e) {
                    log.error("推送消息失败: logId={}", logId, e);
                }
            }
        }
    }
}
