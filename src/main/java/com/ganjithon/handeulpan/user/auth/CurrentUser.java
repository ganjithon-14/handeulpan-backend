package com.ganjithon.handeulpan.user.auth;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 컨트롤러 파라미터에 붙이면 X-Device-Token 헤더로 찾은 {@link com.ganjithon.handeulpan.user.User}가 들어옵니다.
 * 헤더가 없거나 모르는 토큰이면 401 INVALID_DEVICE_TOKEN.
 *
 * <pre>{@code
 * @PutMapping("/api/sheep/{id}/bubble")
 * public BubbleResponse post(@CurrentUser User me, ...) { ... }
 * }</pre>
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface CurrentUser {
}
