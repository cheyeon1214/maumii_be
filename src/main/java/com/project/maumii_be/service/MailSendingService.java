package com.project.maumii_be.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Random;

@Service
@RequiredArgsConstructor
@Slf4j
public class MailSendingService {
    private final JavaMailSender javaMailSender;
    private final StringRedisTemplate redis;

    private static final Duration CODE_EXPIRE_TIME = Duration.ofMinutes(5);

    private void sendEmail(String toEmail, String code) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject("마음이 보호자 이메일 인증 코드");
        message.setText("인증 코드: " + code + "\n5분 이내로 입력해주세요.");
        javaMailSender.send(message);
        log.info("[mail] 인증코드 발송 완료: {}", toEmail);
    }

    private String generateCode() {
        return String.format("%06d", new Random().nextInt(1000000));
    }

    // 인증코드 발송 + Redis 저장 (5분 TTL)
    public void sendVerificationCode(String email) {
        String code = generateCode();
        String key = "email_verify:" + email;

        // 5분 TTL로 저장
        redis.opsForValue().set(key, code, CODE_EXPIRE_TIME);

        sendEmail(email, code);
        log.info("[mail] 인증코드 생성 및 발송: {} (코드: {})", email, code);
    }

    // 인증코드 검증
    public boolean verifyCode(String email, String inputCode) {
        String key = "email_verify:" + email;
        String storedCode = redis.opsForValue().get(key);

        if (storedCode == null) {
            log.warn("[mail] 인증코드 만료 또는 없음: {}", email);
            return false;
        }

        boolean isValid = storedCode.equals(inputCode);
        if (isValid) {
            // 인증 성공 시 코드 삭제
            redis.delete(key);
            log.info("[mail] 인증 성공: {}", email);
        } else {
            log.warn("[mail] 인증코드 불일치: {}", email);
        }

        return isValid;
    }

    // 남은 TTL 확인
    public Duration getTtl(String email) {
        String key = "email_verify:" + email;
        Long sec = redis.getExpire(key);
        log.info("[mail] TTL 확인 - email: {}, 남은시간: {}초", email, sec);
        return (sec == null || sec < 0) ? Duration.ZERO : Duration.ofSeconds(sec);
    }
}