package com.project.maumii_be.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SmsVerifyReq { private String phone; private String code; }