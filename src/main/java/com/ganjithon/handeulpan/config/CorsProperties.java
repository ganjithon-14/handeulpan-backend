package com.ganjithon.handeulpan.config;

import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

/**
 * 프론트(브라우저)가 API를 호출할 수 있는 출처. 배포 서버에서는 환경변수 {@code CORS_ALLOWED_ORIGINS}로 넣습니다.
 * 쉼표로 여러 개를 적을 수 있고 {@code https://*.vercel.app} 같은 와일드카드도 됩니다.
 */
@ConfigurationProperties("handeulpan.cors")
public record CorsProperties(@DefaultValue("http://localhost:5173") List<String> allowedOrigins) {
}
