package com.project.maumii_be.controller;

import com.project.maumii_be.dto.user.SmsSendReq;
import com.project.maumii_be.dto.user.SmsVerifyReq;
import com.project.maumii_be.service.user.MessageSendingService;
import com.project.maumii_be.service.user.SmsService;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "SMS API", description = "Cool SMS 관련 API")
@RequiredArgsConstructor
@Slf4j
public class SmsController {
    private final SmsService smsCodeService;
    private final MessageSendingService messageSendingService; // 네가 만든 SMS 발송 컴포넌트

    // SmsController.java
    @PostMapping("/send")
    public ResponseEntity<?> send(@RequestBody SmsSendReq req) {
        String rawPhone = req.getPhone();
        String normalized = rawPhone.replaceAll("\\D+", "");
        if (normalized.startsWith("82")) normalized = "0" + normalized.substring(2);

        String code = String.format("%06d", (int)(Math.random() * 1_000_000));
        smsCodeService.saveCode(normalized, code, Duration.ofMinutes(3));

        try {
            messageSendingService.sendCode(normalized, code);
            return ResponseEntity.ok(Map.of("ok", true));
        } catch (net.nurigo.sdk.message.exception.NurigoMessageNotReceivedException e) {
            // 개별 실패 사유들
            log.error("[SMS] CoolSMS not received: {}", e.getFailedMessageList());
            return ResponseEntity.status(502).body(Map.of(
                    "ok", false,
                    "provider", "coolsms",
                    "errors", e.getFailedMessageList()
            ));
        } catch (Exception e) {
            log.error("[SMS] send error", e);
            return ResponseEntity.status(500).body(Map.of(
                    "ok", false,
                    "message", "문자 발송 중 오류",
                    "detail", e.getMessage()
            ));
        }
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