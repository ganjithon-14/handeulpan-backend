package com.ganjithon.handeulpan.config;

/** BE-B 기능에서 던지는 예외. {@link ApiExceptionHandler}가 {@code {code, message}} 응답으로 바꿉니다. */
public class ApiException extends RuntimeException {

    private final ApiError error;

    public ApiException(ApiError error) {
        super(error.name());
        this.error = error;
    }

    public ApiError getError() {
        return error;
    }
}
