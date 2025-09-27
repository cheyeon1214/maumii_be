package com.project.maumii_be.service.user;

import lombok.RequiredArgsConstructor;
import net.nurigo.sdk.message.exception.NurigoEmptyResponseException;
import net.nurigo.sdk.message.exception.NurigoMessageNotReceivedException;
import net.nurigo.sdk.message.exception.NurigoUnknownException;
import net.nurigo.sdk.message.model.Message;
import net.nurigo.sdk.message.service.DefaultMessageService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MessageSendingService {
    private final DefaultMessageService messageService;

    @Value("${coolsms.fromnumber}")
    private String fromNumber;

    public void sendCode(String phone, String code)
            throws NurigoMessageNotReceivedException, NurigoEmptyResponseException, NurigoUnknownException {
        Message msg = new Message();
        msg.setFrom(fromNumber);   // 발신번호
        msg.setTo(phone);          // 수신번호
        msg.setText("[마음이] 인증번호: " + code);

        messageService.send(msg);  // CoolSMS SDK로 발송
    }
}