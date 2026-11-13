package com.ganjithon.handeulpan.auth.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * 가입 요청.
 *
 * <p>닉네임은 앞뒤 공백 없이 2~10자(이모지 한 글자는 1자)이고 중복될 수 없습니다. 양 이름과는 별개입니다.
 * 이메일은 형식만 확인하고(인증 메일 없음) 대소문자는 구분하지 않습니다.
 * 비밀번호는 공백 없는 영문·숫자·특수문자 8~72자입니다. 72는 BCrypt가 처리하는 최대 바이트 수라서,
 * 한글처럼 여러 바이트인 글자는 받지 않습니다.
 * 로그인에서는 형식 검증 대신 일치 여부만 보므로 {@link LoginRequest}를 따로 둡니다.
 */
public record SignupRequest(
        @NotNull @Pattern(regexp = "^\\S(?:.{0,8}\\S)$") String nickname,
        @NotNull @Size(max = 100) @Pattern(regexp = "^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$") String email,
        @NotNull @Pattern(regexp = "^[\\x21-\\x7E]{8,72}$") String password) {
}
