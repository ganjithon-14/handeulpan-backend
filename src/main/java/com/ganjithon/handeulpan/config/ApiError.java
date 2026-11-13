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
    DUPLICATE_USERNAME(HttpStatus.CONFLICT, "이미 사용 중인 아이디예요."),
    INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED, "아이디 또는 비밀번호가 올바르지 않아요."),
    ALREADY_HAS_ACCOUNT(HttpStatus.CONFLICT, "이미 계정이 있어요. 로그인해 주세요.");

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
