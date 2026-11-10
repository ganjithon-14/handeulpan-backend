package com.ganjithon.handeulpan.config;

/** 모든 에러 응답의 본문. 예: {@code {"code": "PASTURE_FULL", "message": "이 들판은 가득 찼어요."}} */
public record ApiErrorResponse(String code, String message) {
}
