package com.project.maumii_be.dto.user;

import lombok.Data;


public record AccountUpdateReq(
        String uPhone,          // null/blank면 변경 안 함
        String uPwd             // null/blank면 변경 안 함 (서버에서 인코딩)
) {}

