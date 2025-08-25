// com.project.backend.auth.RzTokenService
package com.project.maumii_be.auth;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

@Slf4j
@Service
@RequiredArgsConstructor
public class RzTokenService {

    private final RestTemplate rest = new RestTemplate();
    private final ObjectMapper om = new ObjectMapper();

    @Value("${returnzero.oauth.token-url}")
    private String tokenUrl;
    @Value("${returnzero.oauth.client-id}")
    private String clientId;
    @Value("${returnzero.oauth.client-secret}")
    private String clientSecret;

    private final AtomicReference<String> cachedToken = new AtomicReference<>();
    private final AtomicReference<Instant> expiresAt = new AtomicReference<>(Instant.EPOCH);

    public String getAccessToken() {
        Instant now = Instant.now();
        if (cachedToken.get() != null && now.isBefore(expiresAt.get().minusSeconds(60))) {
            return cachedToken.get();
        }

        // 필수 값 확인
        if (isBlank(clientId) || isBlank(clientSecret)) {
            throw new IllegalStateException("RTZR clientId/secret is empty. Check application.properties");
        }

        // === JSON (PascalCase: ClientID / ClientSecret) ===
        try {
            HttpHeaders h = new HttpHeaders();
            h.setContentType(MediaType.APPLICATION_JSON);
            h.setAccept(List.of(MediaType.APPLICATION_JSON));

            ObjectNode body = om.createObjectNode()
                    .put("ClientID", clientId)
                    .put("ClientSecret", clientSecret);

            ResponseEntity<String> res =
                    rest.postForEntity(tokenUrl, new HttpEntity<>(body.toString(), h), String.class);

            return parseAndCache(res.getBody(), now);
        } catch (HttpStatusCodeException e) {
            log.error("[RTZR] JSON(PascalCase) token request failed: {} {}",
                    e.getStatusCode(), safe(e.getResponseBodyAsString()));
            throw e;
        } catch (Exception e) {
            log.error("[RTZR] JSON(PascalCase) token request error", e);
            throw new RuntimeException(e);
        }
    }

    private String parseAndCache(String body, Instant now) {
        try {
            JsonNode json = om.readTree(body);
            String token = json.path("access_token").asText(null);
            if (token == null) throw new IllegalStateException("No access_token in: " + body);
            long expEpoch = json.path("expire_at").asLong(0);
            Instant exp = expEpoch > 0 ? Instant.ofEpochSecond(expEpoch) : now.plusSeconds(6*60*60);
            cachedToken.set(token);
            expiresAt.set(exp);
            return token;
        } catch (Exception ex) {
            log.error("[RTZR] parse token failed: {}", body, ex);
            throw new RuntimeException(ex);
        }
    }

    private static boolean isBlank(String s){ return s == null || s.isBlank(); }
    private static String safe(String s){ return (s == null || s.isBlank()) ? "<empty>" : s; }
}