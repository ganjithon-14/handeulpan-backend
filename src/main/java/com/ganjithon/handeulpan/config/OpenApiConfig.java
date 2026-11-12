package com.ganjithon.handeulpan.config;

import com.ganjithon.handeulpan.user.auth.CurrentUser;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.utils.SpringDocUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Swagger UI: /swagger-ui/index.html
 * 오른쪽 위 Authorize에 기기 토큰(POST /api/users로 받은 값)을 넣으면 이후 요청에 X-Device-Token 헤더가 붙습니다.
 */
@Configuration
public class OpenApiConfig {

    static final String DEVICE_TOKEN = "DeviceToken";

    static {
        // @CurrentUser User me 는 헤더로 채워지는 값이라 Swagger의 요청 파라미터로 보이지 않게 합니다.
        SpringDocUtils.getConfig().addAnnotationsToIgnore(CurrentUser.class);
    }

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("한 들판 API")
                        .description("멀리 있어도, 한 들판에. 인증은 헤더 X-Device-Token (POST /api/users로 발급).")
                        .version("MVP"))
                .components(new Components().addSecuritySchemes(DEVICE_TOKEN, new SecurityScheme()
                        .type(SecurityScheme.Type.APIKEY)
                        .in(SecurityScheme.In.HEADER)
                        .name("X-Device-Token")))
                .addSecurityItem(new SecurityRequirement().addList(DEVICE_TOKEN));
    }
}
