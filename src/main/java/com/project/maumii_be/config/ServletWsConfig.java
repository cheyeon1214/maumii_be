package com.project.maumii_be.config;

import com.project.maumii_be.websocket.ServletSttProxyHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.springframework.web.socket.handler.TextWebSocketHandler;

@Configuration
@EnableWebSocket
@RequiredArgsConstructor
public class ServletWsConfig implements WebSocketConfigurer {
    private final ServletSttProxyHandler sttHandler;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(sttHandler, "/ws/stt")
                .setAllowedOrigins("http://localhost:5050", "http://127.0.0.1:5050");

        registry.addHandler(echoHandler(), "/ws/echo")
                .setAllowedOrigins("http://localhost:5050", "http://127.0.0.1:5050");
    }

    @Bean
    public TextWebSocketHandler echoHandler() {
        return new TextWebSocketHandler() {
            @Override
            public void afterConnectionEstablished(WebSocketSession s) throws Exception {
                s.sendMessage(new TextMessage("{\"type\":\"ready\",\"msg\":\"hello from server\"}"));
            }
            @Override
            protected void handleTextMessage(WebSocketSession s, TextMessage msg) throws Exception {
                s.sendMessage(new TextMessage("{\"echo\":" + msg.getPayload() + "}"));
            }
        };
    }
}