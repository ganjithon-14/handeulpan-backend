package com.ganjithon.handeulpan.auth.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

/**
 * 가입 요청.
 *
 * <p>아이디는 영문·숫자 4~20자(대소문자 구분 없음), 비밀번호는 공백 없는 영문·숫자·특수문자 8~72자입니다.
 * 72는 BCrypt가 처리하는 최대 바이트 수라서, 한글처럼 여러 바이트인 글자는 받지 않습니다.
 * 로그인에서는 형식 검증 대신 일치 여부만 보므로 {@link LoginRequest}를 따로 둡니다.
 */
public record AuthRequest(
        @NotNull @Pattern(regexp = "^[A-Za-z0-9]{4,20}$") String username,
        @NotNull @Pattern(regexp = "^[\\x21-\\x7E]{8,72}$") String password) {
}
