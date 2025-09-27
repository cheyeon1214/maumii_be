package com.project.maumii_be.dto.user;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class SmsSendReq {
    private String phone;   // 사용자 휴대폰 번호
}