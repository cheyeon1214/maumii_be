// src/main/java/com/project/backend/websocket/DemoEchoHandler.java
package com.project.maumii_be.websocket;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.TextWebSocketHandler;

@Slf4j
@Component
public class DemoEchoHandler extends TextWebSocketHandler {

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        log.info("[WS] connected: {}", session.getId());
        // 연결되자마자 프론트로 'ready' 한 줄 보내기
        session.sendMessage(new TextMessage("{\"type\":\"ready\",\"msg\":\"hello from server\"}"));
    }

    // 프론트에서 텍스트를 보내면 그대로 에코
    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        log.info("[WS] text in: {}", message.getPayload());
        session.sendMessage(new TextMessage("{\"type\":\"echo\",\"data\":" + message.getPayload() + "}"));
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        log.warn("[WS] transport error", exception);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        log.info("[WS] closed: id={} code={} reason={}", session.getId(), status.getCode(), status.getReason());
    }
}