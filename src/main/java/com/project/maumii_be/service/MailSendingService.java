package com.project.maumii_be.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class MailSendingService {
    final JavaMailSender javaMailSender;
    final StringRedisTemplate redis;

    public void sendCode(String pEmail){
        String code = generateCode();
        redis.opsForValue().set(pEmail, code);

    }

    private String generateCode() {
        String code=null;
        return code;
    }
}
