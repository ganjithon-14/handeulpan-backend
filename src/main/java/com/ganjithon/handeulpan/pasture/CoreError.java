package com.ganjithon.handeulpan.pasture;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

/**
 * BE-A가 던지는 에러 코드. 코드 이름은 docs/api/common.md 에러 코드 표를 따릅니다.
 *
 * <p>공통 에러 형식({code, message})과 전역 핸들러는 BE-B 담당입니다. 그게 들어오기 전까지는
 * {@link ResponseStatusException}의 reason에 code를 담아 던지고, 들어오면 BE-B의 예외 타입으로 바꿉니다.
 */
public enum CoreError {

    INVALID_DEVICE_TOKEN(HttpStatus.UNAUTHORIZED, "기기 토큰이 없거나 올바르지 않아요."),
    PASTURE_NOT_FOUND(HttpStatus.NOT_FOUND, "들판을 찾을 수 없어요."),
    SHEEP_NOT_FOUND(HttpStatus.NOT_FOUND, "양을 찾을 수 없어요."),
    NOT_PASTURE_MEMBER(HttpStatus.FORBIDDEN, "이 들판의 멤버가 아니에요."),
    NOT_MY_SHEEP(HttpStatus.FORBIDDEN, "내 양만 할 수 있어요."),
    PASTURE_FULL(HttpStatus.CONFLICT, "이 들판은 가득 찼어요."),
    ALREADY_JOINED(HttpStatus.CONFLICT, "이미 이 들판에 합류했어요."),
    INVALID_DEMO_KEY(HttpStatus.FORBIDDEN, "데모 키가 올바르지 않아요.");

    private final HttpStatus status;
    private final String message;

    CoreError(HttpStatus status, String message) {
        this.status = status;
        this.message = message;
    }

    public HttpStatus status() {
        return status;
    }

    public String message() {
        return message;
    }

    public ResponseStatusException exception() {
        return new ResponseStatusException(status, name());
    }
}
