package com.project.maumii_be.apipayload.status;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ErrorStatus {

    // COMMON
    _INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "COMMON500", "서버 에러, 관리자에게 문의 바랍니다"),
    _BAD_REQUEST(HttpStatus.BAD_REQUEST,"COMMON400","잘못된 요청입니다"),
    _UNAUTHORIZED(HttpStatus.UNAUTHORIZED,"COMMON401","인증이 필요합니다"),
    _FORBIDDEN(HttpStatus.FORBIDDEN, "COMMON403", "금지된 요청입니다"),

    // USER
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "USER4001", "사용자 정보를 찾을 수 없습니다"),
    PASSWORD_NOT_MATCHED(HttpStatus.UNAUTHORIZED, "USER4002", "비밀번호가 일치하지 않습니다"),
    DUPLICATED_NICKNAME(HttpStatus.BAD_REQUEST, "USER4003", "이미 사용 중인 닉네임입니다"),
    EMAIL_NOT_FOUND(HttpStatus.NOT_FOUND, "USER4004", "이메일을 찾을 수 없습니다"),
    LOGIN_FAILURE(HttpStatus.UNAUTHORIZED, "USER4005", "아이디나 비밀번호가 일치하지 않습니다"),
    OWN_PROJECT(HttpStatus.BAD_REQUEST, "USER4007", "자신의 프로젝트에는 지원할 수 없습니다"),

    // PROJECT
    PROJECT_NOT_FOUND(HttpStatus.NOT_FOUND, "PRO4001", "프로젝트 정보를 찾을 수 없습니다"),
    PROJECT_POSITION_NOT_FOUND(HttpStatus.NOT_FOUND, "PRO4002", "프로젝트 포지션 정보를 찾을 수 없습니다"),
    ALREADY_MATCHING_END_POSITION(HttpStatus.BAD_REQUEST, "PRO4003", "해당 포지션에 대한 신청이 마감되었습니다"),
    ALREADY_COMPLETED_PROJECT(HttpStatus.BAD_REQUEST, "PRO4004", "프로젝트의 모집이 마감되었습니다"),
    APPLICATION_NOT_FOUND(HttpStatus.NOT_FOUND, "PRO4005", "프로젝트 지원 정보를 찾을 수 없습니다"),
    APPLICATION_STATE_FINALIZED(HttpStatus.BAD_REQUEST, "PRO4006", "이미 지원자의 처리를 완료하였습니다"),

    // JWT
    TOKEN_INVALID(HttpStatus.UNAUTHORIZED, "JWT4001", "token이 유효하지 않습니다"),

    // POSITION
    POSITION_NOT_FOUND(HttpStatus.NOT_FOUND, "POS4001", "포지션 정보를 찾을 수 없습니다"),

    // PLATFORM
    PLATFORM_NOT_FOUND(HttpStatus.NOT_FOUND, "PLAT4001", "플랫폼 정보를 찾을 수 없습니다"),

    // TECHNOLOGY
    TECH_NOT_FOUND(HttpStatus.BAD_REQUEST, "TECH4001", "기술 정보를 찾을 수 없습니다"),

    // PAGE
    PAGE_REQUIRED(HttpStatus.BAD_REQUEST, "PAGE4001", "페이지는 필수 입력값입니다"),
    PAGE_NOT_POSITIVE(HttpStatus.BAD_REQUEST, "PAGE4002", "페이지 값은 양수이어야 합니다"),

    //community
    COMMUNITY_NOT_FOUND(HttpStatus.NOT_FOUND, "COMMUNITY4001", "게시글을 찾을 수 없습니다"),

    // UNIV CERT
    EMAIL_EXISTS(HttpStatus.CONFLICT, "UNIV4001", "이미 인증된 이메일입니다. 로그인하세요."),
    INVALID_UNIV_NAME(HttpStatus.BAD_REQUEST, "UNIV4002", "인증이 불가능한 학교명입니다"),
    STUDENT_AUTH_FAILED(HttpStatus.BAD_REQUEST, "UNIV4003", "재학생 인증에 실패하였습니다"),
    INVALID_CODE(HttpStatus.BAD_REQUEST, "UNIV4004", "인증 코드가 일치하지 않습니다"),
    ALREADY_AUTHENTICATED(HttpStatus.CONFLICT, "UNIV4005", "이미 인증된 사용자입니다. 회원가입하세요."),

    // FILE UPLOAD
    EMPTY_FILE(HttpStatus.BAD_REQUEST, "FILE4001", "빈 파일입니다"),
    INVALID_CONTENT_TYPE(HttpStatus.BAD_REQUEST, "FILE4002", "이미지 파일만 업로드할 수 있습니다"),
    FILE_UPLOAD_FAILED(HttpStatus.BAD_REQUEST, "FILE4003", "파일을 업로드할 수 없습니다"),

    // RESUME
    RESUME_NOT_FOUND(HttpStatus.NOT_FOUND, "RES4001", "이력서를 찾을 수 없습니다");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
