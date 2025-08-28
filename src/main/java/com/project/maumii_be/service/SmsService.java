package com.project.maumii_be.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
@Slf4j
public class SmsService {
    private final StringRedisTemplate redis; // 키/값 모두 String

    private String normalizePhone(String raw) {
        if (raw == null) return "";
        String digits = raw.replaceAll("\\D+", "");
        if (digits.startsWith("82")) {
            return "0" + digits.substring(2);
        }
        return digits;
    }
    private String keyOf(String phone) { return "sms:code:" + normalizePhone(phone); }

    public void saveCode(String phone, String code, Duration ttl) {
        String k = keyOf(phone);
        log.info("[SMS] SAVE phoneRaw={}, phoneNorm={}, key={}, code={}, ttlSec={}",
                phone, normalizePhone(phone), k, code, ttl.toSeconds());
        redis.opsForValue().set(k, code, ttl);
        Long remain = redis.getExpire(k);
        log.info("[SMS] SAVE after set TTL={}s", remain);
    }

    public boolean verifyAndConsume(String phone, String input) {
        String k = keyOf(phone);
        String saved = redis.opsForValue().get(k);
        log.info("[SMS] VERIFY key={}, saved={}, input={}", k, saved, input);

        boolean ok = (saved != null && saved.equals(input));
        if (ok) {
            redis.delete(k);
            log.info("[SMS] VERIFY ok → deleted key={}", k);
        } else {
            Long ttl = redis.getExpire(k);
            log.warn("[SMS] VERIFY fail (mismatch or expired). key={}, ttl={}s", k, ttl);
        }
        return ok;
    }

    public Duration ttl(String phone) {
        String k = keyOf(phone);
        Long sec = redis.getExpire(k);
        log.info("[SMS] TTL key={}, remain={}s", k, sec);
        return (sec == null || sec < 0) ? Duration.ZERO : Duration.ofSeconds(sec);
    }
}