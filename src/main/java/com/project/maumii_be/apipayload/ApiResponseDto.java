package com.project.maumii_be.apipayload;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.project.maumii_be.apipayload.status.ErrorStatus;
import com.project.maumii_be.apipayload.status.SuccessStatus;
import com.project.maumii_be.apipayload.status.SuccessStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@JsonPropertyOrder({"isSuccess", "code", "message", "result"})
public class ApiResponseDto<T> {

    private final Boolean isSuccess;
    private final String code;
    private final String message;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private T result;

    // 200 OK 시 호출
    public static <T> ApiResponseDto<T> onSuccess(T result) {
        return new ApiResponseDto<>(
                true,
                SuccessStatus._OK.getCode(),
                SuccessStatus._OK.getMessage(),
                result
        );
    }

    // 200 OK 외 성공 처리 시 호출
    public static <T> ApiResponseDto<T> of(SuccessStatus successStatus, T result) {
        return new ApiResponseDto<>(
                true,
                successStatus.getCode(),
                successStatus.getMessage(),
                result
        );
    }

    // 에러 발생 시 호출
    public static <T> ApiResponseDto<T> onFailure(ErrorStatus errorStatus, T data) {
        return new ApiResponseDto<>(
                false,
                errorStatus.getCode(),
                errorStatus.getMessage(),
                data
        );
    }
}
