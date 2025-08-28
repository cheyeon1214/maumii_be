package com.project.maumii_be.controller;

import com.project.maumii_be.dto.SmsSendReq;
import com.project.maumii_be.dto.SmsVerifyReq;
import com.project.maumii_be.service.MessageSendingService;
import com.project.maumii_be.service.SmsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;
import java.util.Map;

@RestController
@RequestMapping("/api/sms")
@RequiredArgsConstructor
@Slf4j
public class SmsController {
    private final SmsService smsCodeService;
    private final MessageSendingService messageSendingService; // 네가 만든 SMS 발송 컴포넌트

    @PostMapping("/send")
    public ResponseEntity<?> send(@RequestBody SmsSendReq req) {
        String code = String.format("%06d", (int)(Math.random() * 1_000_000));
        smsCodeService.saveCode(req.getPhone(), code, Duration.ofMinutes(3));

        try {
            messageSendingService.sendCode(req.getPhone(), code);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("문자 발송 실패: " + e.getMessage());
        }

        return ResponseEntity.ok("전송 완료");
    }

    @PostMapping("/verify")
    public ResponseEntity<?> verify(@RequestBody SmsVerifyReq req) {
        log.info("[SMS] /verify request phone={}, code={}", req.getPhone(), req.getCode());
        boolean ok = smsCodeService.verifyAndConsume(req.getPhone(), req.getCode());
        if (ok) {
            log.info("[SMS] /verify OK");
            return ResponseEntity.ok(Map.of("ok", true));
        } else {
            log.warn("[SMS] /verify FAIL");
            return ResponseEntity.status(400).body(Map.of("ok", false, "message", "인증 실패"));
        }
    }
}