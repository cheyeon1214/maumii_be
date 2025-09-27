package com.project.maumii_be.apipayload.status;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum SuccessStatus {

    // COMMON
    _OK(HttpStatus.OK, "COMMON200", "성공입니다"),

    _CREATED(HttpStatus.CREATED, "COMMON201", "새 리소스가 성공적으로 생성되었습니다"),
    _DELETED(HttpStatus.OK, "COMMON202", "성공적으로 삭제되었습니다"),
    _UPDATED(HttpStatus.OK,"COMMON203" , "성공적으로 수정되었습니다" );



    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
