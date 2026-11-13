package com.ganjithon.handeulpan.config;

import org.springframework.http.HttpStatus;

/**
 * BE-B가 던지는 에러 코드. 이름은 docs/api/common.md 에러 코드 표를 따릅니다.
 * (BE-A의 에러 코드는 {@link com.ganjithon.handeulpan.pasture.CoreError}에 있습니다.)
 */
public enum ApiError {

    VALIDATION_FAILED(HttpStatus.BAD_REQUEST, "요청 형식이 올바르지 않아요."),
    INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "잠시 후 다시 시도해 주세요."),

    // 말풍선
    BUBBLE_TEXT_INVALID(HttpStatus.BAD_REQUEST, "말풍선은 1~20자로 적어 주세요."),
    BUBBLE_NOT_FOUND(HttpStatus.NOT_FOUND, "지울 말풍선이 없어요."),

    // 가입·로그인
    DUPLICATE_EMAIL(HttpStatus.CONFLICT, "이미 가입된 이메일이에요."),
    INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED, "이메일 또는 비밀번호가 올바르지 않아요."),

    // 목도리
    SELF(HttpStatus.BAD_REQUEST, "내 양에게는 목도리를 보낼 수 없어요."),
    ALREADY_SENT_TODAY(HttpStatus.CONFLICT, "오늘은 이미 목도리를 보냈어요."),
    SCARF_NOT_FOUND(HttpStatus.NOT_FOUND, "받은 목도리가 아니에요.");

    private final HttpStatus status;
    private final String message;

    ApiError(HttpStatus status, String message) {
        this.status = status;
        this.message = message;
    }

    public HttpStatus status() {
        return status;
    }

    public String message() {
        return message;
    }

    public ApiException exception() {
        return new ApiException(this);
    }
}
