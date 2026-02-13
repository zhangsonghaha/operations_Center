package com.ruoyi.web.socket;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.WebSocketMessage;
import org.springframework.web.socket.WebSocketSession;
import com.ruoyi.web.service.WebSshService;

/**
 * WebSSH WebSocket处理器
 */
@Component
public class WebSshWebSocketHandler implements WebSocketHandler
{
    private static final Logger log = LoggerFactory.getLogger(WebSshWebSocketHandler.class);

    @Autowired
    private WebSshService webSshService;

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception
    {
        log.info("WebSSH连接建立: {}", session.getId());
        webSshService.initConnection(session);
    }

    @Override
    public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) throws Exception
    {
        if (message instanceof TextMessage) {
            webSshService.recvHandle(((TextMessage) message).getPayload(), session);
        }
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception
    {
        log.error("WebSSH连接异常", exception);
        webSshService.close(session);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) throws Exception
    {
        log.info("WebSSH连接关闭: {}", session.getId());
        webSshService.close(session);
    }

    @Override
    public boolean supportsPartialMessages()
    {
        return false;
    }
}
