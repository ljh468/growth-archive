package com.growtharchive.exception;

import org.springframework.http.HttpStatus;

public enum ErrorCode {
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "인증이 필요합니다."),
    FORBIDDEN(HttpStatus.FORBIDDEN, "접근 권한이 없습니다."),
    INVALID_INVITE_CODE(HttpStatus.BAD_REQUEST, "초대코드가 올바르지 않습니다."),
    TERMS_NOT_AGREED(HttpStatus.BAD_REQUEST, "약관과 개인정보처리방침에 동의해 주세요."),
    ONBOARDING_REQUIRED(HttpStatus.FORBIDDEN, "온보딩을 완료해 주세요."),
    ALREADY_ONBOARDED(HttpStatus.CONFLICT, "이미 온보딩이 완료되었습니다."),
    DUPLICATE_NICKNAME(HttpStatus.CONFLICT, "이미 사용 중인 닉네임입니다."),
    MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "회원을 찾을 수 없습니다."),
    BOOK_NOT_FOUND(HttpStatus.NOT_FOUND, "책을 찾을 수 없습니다."),
    READING_RECORD_NOT_FOUND(HttpStatus.NOT_FOUND, "독서기록을 찾을 수 없습니다."),
    MEETING_NOT_FOUND(HttpStatus.NOT_FOUND, "모임을 찾을 수 없습니다."),
    MEETING_CAPACITY_FULL(HttpStatus.CONFLICT, "모임 정원이 마감되었습니다."),
    KAKAO_NOT_CONFIGURED(HttpStatus.SERVICE_UNAVAILABLE, "카카오 로그인이 아직 설정되지 않았습니다."),
    VALIDATION_ERROR(HttpStatus.BAD_REQUEST, "입력값을 확인해 주세요."),
    INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "요청을 처리하지 못했습니다.");

    private final HttpStatus status;
    private final String message;

    ErrorCode(HttpStatus status, String message) {
        this.status = status;
        this.message = message;
    }

    public HttpStatus status() {
        return status;
    }

    public String message() {
        return message;
    }
}
