package com.ganjithon.handeulpan.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

/** 공통 에러 형식 · CORS · Swagger (BE-B 공통 설정) */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class CommonApiTest {

    @Autowired
    MockMvc mvc;

    // ---------- 에러 형식 {code, message} ----------

    @Test
    void BE_A_에러는_코드와_메시지로_내려간다() throws Exception {
        mvc.perform(get("/api/pastures"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("INVALID_DEVICE_TOKEN"))
                .andExpect(jsonPath("$.message").value("기기 토큰이 없거나 올바르지 않아요."));
    }

    @Test
    void 존재하지_않는_들판은_404_PASTURE_NOT_FOUND() throws Exception {
        String token = signup("두부");
        mvc.perform(get("/api/pastures/{id}", "no-such-pasture").header("X-Device-Token", token))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("PASTURE_NOT_FOUND"))
                .andExpect(jsonPath("$.message").isNotEmpty());
    }

    @Test
    void 검증에_실패하면_400_VALIDATION_FAILED() throws Exception {
        String token = signup("두부");
        mvc.perform(post("/api/pastures").header("X-Device-Token", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"woolColor\":\"CREAM\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"))
                .andExpect(jsonPath("$.message").isNotEmpty());
    }

    @Test
    void 깨진_JSON도_400_VALIDATION_FAILED() throws Exception {
        String token = signup("두부");
        mvc.perform(post("/api/pastures").header("X-Device-Token", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{not json"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"));
    }

    @Test
    void 없는_주소는_404_NOT_FOUND() throws Exception {
        mvc.perform(get("/api/no-such-endpoint"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("NOT_FOUND"));
    }

    @Test
    void 지원하지_않는_메서드는_405() throws Exception {
        mvc.perform(get("/api/auth/login"))
                .andExpect(status().isMethodNotAllowed())
                .andExpect(jsonPath("$.code").value("METHOD_NOT_ALLOWED"));
    }

    @Test
    void ApiException은_자기_상태와_코드로_내려간다() {
        var response = new ApiExceptionHandler().handleApi(ApiError.VALIDATION_FAILED.exception());

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isEqualTo(
                new ApiErrorResponse("VALIDATION_FAILED", ApiError.VALIDATION_FAILED.message()));
    }

    @Test
    void 모르는_reason의_ResponseStatusException은_상태_이름으로_내려간다() {
        var response = new ApiExceptionHandler()
                .handleResponseStatus(new ResponseStatusException(HttpStatus.CONFLICT, "something-else"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody().code()).isEqualTo("CONFLICT");
    }

    @Test
    void 예상_못_한_예외는_500_INTERNAL_ERROR이고_내부_메시지는_숨긴다() {
        var response = new ApiExceptionHandler().handleUnexpected(new IllegalStateException("db password=secret"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody().code()).isEqualTo("INTERNAL_ERROR");
        assertThat(response.getBody().message()).doesNotContain("secret");
    }

    // ---------- CORS ----------

    @Test
    void 허용된_출처의_preflight는_통과하고_커스텀_헤더를_허용한다() throws Exception {
        mvc.perform(options("/api/pastures")
                        .header("Origin", "http://localhost:5173")
                        .header("Access-Control-Request-Method", "POST")
                        .header("Access-Control-Request-Headers", "x-device-token,content-type"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:5173"))
                .andExpect(header().string("Access-Control-Allow-Headers",
                        org.hamcrest.Matchers.containsStringIgnoringCase("X-Device-Token")));
    }

    @Test
    void 허용되지_않은_출처의_preflight는_거부된다() throws Exception {
        mvc.perform(options("/api/pastures")
                        .header("Origin", "https://evil.example.com")
                        .header("Access-Control-Request-Method", "POST"))
                .andExpect(status().isForbidden());
    }

    // ---------- Swagger ----------

    @Test
    void Swagger_문서에_기기_토큰_헤더가_있고_CurrentUser는_파라미터로_보이지_않는다() throws Exception {
        mvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.components.securitySchemes.DeviceToken.name").value("X-Device-Token"))
                .andExpect(jsonPath("$.components.securitySchemes.DeviceToken.in").value("header"))
                // 들판 만들기는 문서에 있고 본문(request body)만 받는다. @CurrentUser User가 파라미터로 끼면 안 된다.
                .andExpect(jsonPath("$.paths['/api/pastures'].post.requestBody").exists())
                .andExpect(jsonPath("$.paths['/api/pastures'].post.parameters").doesNotExist())
                // 회원 전용: 가입은 문서에 있고(양 이름·이메일·비밀번호), 토큰만 발급하는 API와 합류 때 양 이름 입력은 없다.
                .andExpect(jsonPath("$.paths['/api/auth/signup'].post").exists())
                .andExpect(jsonPath("$.components.schemas.SignupRequest.properties.sheepName").exists())
                .andExpect(jsonPath("$.paths['/api/users']").doesNotExist())
                .andExpect(jsonPath("$.components.schemas.JoinPastureRequest.properties.sheepName").doesNotExist());
    }

    private String signup(String sheepName) throws Exception {
        return com.ganjithon.handeulpan.support.TestUsers.signup(mvc, sheepName);
    }
}
