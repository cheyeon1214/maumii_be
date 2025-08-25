// com.project.backend.websocket.ServletSttProxyHandler
package com.project.maumii_be.websocket;

import com.project.maumii_be.auth.RzTokenService;
import com.project.maumii_be.auth.RzTokenService;
import jakarta.websocket.*;
import jakarta.websocket.ClientEndpointConfig.Configurator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.websocket.WsWebSocketContainer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.AbstractWebSocketHandler;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Slf4j
@Component
@RequiredArgsConstructor
public class ServletSttProxyHandler extends AbstractWebSocketHandler {

    private final RzTokenService tokenService;

    @Value("${returnzero.ws.url}")
    private String rzWsUrl;

    @Value("${stt.default.sample-rate:16000}") private int defaultSampleRate;
    @Value("${stt.default.encoding:OGG_OPUS}") private String defaultEncoding;
    @Value("${stt.default.use-itn:true}") private boolean defaultUseItn;
    @Value("${stt.default.use-disfluency-filter:true}") private boolean defaultUseDisfluency;
    @Value("${stt.default.use-profanity-filter:false}") private boolean defaultUseProfanity;
    @Value("${stt.default.model-name:sommers_ko}") private String defaultModelName;
    @Value("${stt.default.domain:CALL}") private String defaultDomain;

    private static final Set<String> ALLOWED_PARAMS = Set.of(
            "sample_rate","encoding","use_itn","use_disfluency_filter",
            "use_profanity_filter","use_punctuation","model_name",
            "domain","keywords","language"
    );

    @Override
    public void afterConnectionEstablished(WebSocketSession client) throws Exception {
        log.info("[/ws/stt] handshake OK from {}", client.getRemoteAddress());

        // 1) 쿼리 구성
        Map<String, List<String>> reqParams = client.getUri().getQuery() == null
                ? Map.of()
                : UriComponentsBuilder.fromUri(client.getUri()).build().getQueryParams();

        StringBuilder qs = new StringBuilder("?");
        addIfAbsent(qs, "sample_rate", reqParams, String.valueOf(defaultSampleRate));
        addIfAbsent(qs, "encoding", reqParams, defaultEncoding);
        addIfAbsent(qs, "use_itn", reqParams, String.valueOf(defaultUseItn));
        addIfAbsent(qs, "use_disfluency_filter", reqParams, String.valueOf(defaultUseDisfluency));
        addIfAbsent(qs, "use_profanity_filter", reqParams, String.valueOf(defaultUseProfanity));
        addIfAbsent(qs, "model_name", reqParams, defaultModelName);
        addIfAbsent(qs, "domain", reqParams, defaultDomain);
        for (var e : reqParams.entrySet()) {
            if (ALLOWED_PARAMS.contains(e.getKey())) {
                for (String v : e.getValue()) appendParam(qs, e.getKey(), v);
            }
        }

        String token = tokenService.getAccessToken();
        String fullUrl = rzWsUrl + (qs.length() == 1 ? "" : qs.toString());

        // 2) ✅ Tomcat WebSocket 클라이언트로 연결
        ClientEndpointConfig config = ClientEndpointConfig.Builder.create()
                .configurator(new Configurator() {
                    @Override
                    public void beforeRequest(Map<String, List<String>> headers) {
                        headers.put("Authorization", List.of("Bearer " + token));
                    }
                }).build();

        WebSocketContainer container = new WsWebSocketContainer(); // <- 핵심
        Session[] rzHolder = new Session[1];

        Endpoint endpoint = new Endpoint() {
            @Override
            public void onOpen(Session rzSession, EndpointConfig endpointConfig) {
                rzHolder[0] = rzSession;

                rzSession.addMessageHandler(String.class, msg -> {
                    try { client.sendMessage(new TextMessage(msg)); }
                    catch (Exception ex) { log.warn("send text to client failed", ex); }
                });
                rzSession.addMessageHandler(ByteBuffer.class, bytes -> {
                    try { client.sendMessage(new BinaryMessage(bytes)); }
                    catch (Exception ex) { log.warn("send binary to client failed", ex); }
                });
            }
            @Override
            public void onClose(Session session, CloseReason closeReason) {
                try { client.close(); } catch (Exception ignored) {}
            }
            @Override
            public void onError(Session session, Throwable thr) {
                log.error("RTZR ws error", thr);
                try { client.close(CloseStatus.SERVER_ERROR); } catch (Exception ignored) {}
            }
        };

        container.connectToServer(endpoint, config, URI.create(fullUrl));

        // 3) 브라우저 → RTZR 전달을 위해 세션 보관
        client.getAttributes().put("rtzrSession", rzHolder);
    }

    @Override
    protected void handleTextMessage(WebSocketSession client, TextMessage message) throws Exception {
        Session rz = getRz(client);
        if (rz != null && rz.isOpen()) rz.getAsyncRemote().sendText(message.getPayload());
    }
    @Override
    protected void handleBinaryMessage(WebSocketSession client, BinaryMessage message) throws Exception {
        Session rz = getRz(client);
        if (rz != null && rz.isOpen()) rz.getAsyncRemote().sendBinary(message.getPayload());
    }
    @Override
    public void afterConnectionClosed(WebSocketSession client, CloseStatus status) throws Exception {
        Session rz = getRz(client);
        if (rz != null && rz.isOpen()) rz.close();
    }

    private Session getRz(WebSocketSession client) {
        Session[] holder = (Session[]) client.getAttributes().get("rtzrSession");
        return holder == null ? null : holder[0];
    }
    private static void addIfAbsent(StringBuilder qs, String k, Map<String,List<String>> p, String def){
        if (!p.containsKey(k)) appendParam(qs, k, def);
    }
    private static void appendParam(StringBuilder qs, String k, String v){
        if (qs.length() > 1) qs.append('&');
        qs.append(k).append('=').append(v);
    }
}