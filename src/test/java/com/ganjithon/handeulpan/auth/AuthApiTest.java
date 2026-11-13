package com.ganjithon.handeulpan.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

/** 양 이름·이메일·비밀번호 가입 / 로그인 / 로그아웃 (회원 전용, 기기 토큰 UUID 방식 유지) */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class AuthApiTest {

    static final String PASSWORD = "pass1234!";

    @Autowired
    MockMvc mvc;

    // ---------- 가입 ----------

    @Test
    void 가입하면_바로_쓸_수_있는_토큰을_받는다() throws Exception {
        String token = tokenOf(signup("두부", "dubu@test.com", PASSWORD).andExpect(status().isCreated()));

        mvc.perform(get("/api/pastures").header("X-Device-Token", token)).andExpect(status().isOk());
    }

    @Test
    void 가입하지_않은_토큰으로는_아무것도_할_수_없다() throws Exception {
        expectError(mvc.perform(get("/api/pastures")), 401, "INVALID_DEVICE_TOKEN");
        expectError(mvc.perform(get("/api/pastures").header("X-Device-Token", "no-such-token")),
                401, "INVALID_DEVICE_TOKEN");
    }

    @Test
    void 토큰_발급만_하는_API는_없다() throws Exception {
        mvc.perform(post("/api/users")).andExpect(status().isNotFound());
    }

    @Test
    void 가입한_양_이름이_양에_쓰인다() throws Exception {
        String token = tokenOf(signup("두부", "dubu@test.com", PASSWORD));

        String pastureId = createPasture(token);

        mvc.perform(get("/api/pastures/{id}", pastureId).header("X-Device-Token", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sheep[0].name").value("두부"));
    }

    @Test
    void 이메일은_대소문자_구분_없이_중복이면_409() throws Exception {
        signup("두부", "Dubu@Test.com", PASSWORD).andExpect(status().isCreated());

        expectError(signup("콩이", "dubu@test.com", PASSWORD), 409, "DUPLICATE_EMAIL");
        expectError(signup("콩이", "DUBU@TEST.COM", PASSWORD), 409, "DUPLICATE_EMAIL");
    }

    @Test
    void 양_이름이_중복이면_409() throws Exception {
        signup("두부", "dubu@test.com", PASSWORD).andExpect(status().isCreated());

        expectError(signup("두부", "other@test.com", PASSWORD), 409, "DUPLICATE_SHEEP_NAME");
    }

    @Test
    void 양_이름은_앞뒤_공백_없이_2에서_10자() throws Exception {
        signup("두부", "a@test.com", PASSWORD).andExpect(status().isCreated());              // 2자
        signup("열글자열글자열글자열", "b@test.com", PASSWORD).andExpect(status().isCreated());  // 10자
        signup("양 이름", "c@test.com", PASSWORD).andExpect(status().isCreated());            // 가운데 공백은 허용
        signup("🐑🐑", "d@test.com", PASSWORD).andExpect(status().isCreated());              // 이모지는 한 글자로 센다

        expectError(signup("두", "e@test.com", PASSWORD), 400, "VALIDATION_FAILED");                  // 1자
        expectError(signup("열한글자열한글자열한글", "e@test.com", PASSWORD), 400, "VALIDATION_FAILED"); // 11자
        expectError(signup(" 두부", "e@test.com", PASSWORD), 400, "VALIDATION_FAILED");               // 앞 공백
        expectError(signup("두부 ", "e@test.com", PASSWORD), 400, "VALIDATION_FAILED");               // 뒤 공백
    }

    @Test
    void 이메일_형식이_틀리면_400() throws Exception {
        expectError(signup("두부", "dubu", PASSWORD), 400, "VALIDATION_FAILED");
        expectError(signup("두부", "dubu@test", PASSWORD), 400, "VALIDATION_FAILED");
        expectError(signup("두부", "du bu@test.com", PASSWORD), 400, "VALIDATION_FAILED");
        expectError(signup("두부", "a".repeat(100) + "@test.com", PASSWORD), 400, "VALIDATION_FAILED");
    }

    @Test
    void 비밀번호_형식이_틀리면_400() throws Exception {
        expectError(signup("두부", "dubu@test.com", "short1!"), 400, "VALIDATION_FAILED");          // 8자 미만
        expectError(signup("두부", "dubu@test.com", "pass word1"), 400, "VALIDATION_FAILED");       // 공백
        expectError(signup("두부", "dubu@test.com", "비밀번호비밀번호비밀"), 400, "VALIDATION_FAILED"); // 한글
        expectError(mvc.perform(post("/api/auth/signup").contentType(MediaType.APPLICATION_JSON).content("{}")),
                400, "VALIDATION_FAILED");
    }

    @Test
    void 비밀번호는_해시로_저장되고_응답에_나오지_않는다(@Autowired AccountRepository accounts) throws Exception {
        String body = signup("두부", "dubu@test.com", PASSWORD).andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        assertThat(body).doesNotContain(PASSWORD);
        Account account = accounts.findByEmail("dubu@test.com").orElseThrow();
        assertThat(account.getPasswordHash()).isNotEqualTo(PASSWORD).startsWith("$2");
    }

    // ---------- 로그인 ----------

    @Test
    void 로그인하면_새_토큰을_받고_같은_양이_보인다() throws Exception {
        String oldToken = tokenOf(signup("두부", "dubu@test.com", PASSWORD));
        String pastureId = createPasture(oldToken);

        String newToken = tokenOf(login("dubu@test.com", PASSWORD).andExpect(status().isOk()));

        assertThat(newToken).isNotEqualTo(oldToken);
        mvc.perform(get("/api/pastures/{id}", pastureId).header("X-Device-Token", newToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sheep[0].name").value("두부"));
        // 이전 토큰은 더 이상 통하지 않는다 (다른 기기에서 로그인한 것과 같다)
        expectError(mvc.perform(get("/api/pastures").header("X-Device-Token", oldToken)),
                401, "INVALID_DEVICE_TOKEN");
    }

    @Test
    void 로그인_이메일은_대소문자를_가리지_않는다() throws Exception {
        signup("두부", "Dubu@Test.com", PASSWORD).andExpect(status().isCreated());

        login("DUBU@test.COM", PASSWORD).andExpect(status().isOk());
    }

    @Test
    void 틀린_비밀번호와_없는_이메일은_같은_401로_답한다() throws Exception {
        signup("두부", "dubu@test.com", PASSWORD).andExpect(status().isCreated());

        expectError(login("dubu@test.com", "wrong-password1"), 401, "INVALID_CREDENTIALS");
        expectError(login("nobody@test.com", PASSWORD), 401, "INVALID_CREDENTIALS");
    }

    @Test
    void 로그인_본문이_비었으면_400() throws Exception {
        expectError(mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON).content("{}")),
                400, "VALIDATION_FAILED");
    }

    // ---------- 로그아웃 ----------

    @Test
    void 로그아웃하면_204이고_그_토큰은_쓸_수_없다() throws Exception {
        String token = tokenOf(signup("두부", "dubu@test.com", PASSWORD));

        mvc.perform(post("/api/auth/logout").header("X-Device-Token", token)).andExpect(status().isNoContent());

        expectError(mvc.perform(get("/api/pastures").header("X-Device-Token", token)),
                401, "INVALID_DEVICE_TOKEN");
    }

    @Test
    void 로그아웃_뒤_다시_로그인하면_데이터가_그대로_있다() throws Exception {
        String token = tokenOf(signup("두부", "dubu@test.com", PASSWORD));
        String pastureId = createPasture(token);

        mvc.perform(post("/api/auth/logout").header("X-Device-Token", token)).andExpect(status().isNoContent());
        String again = tokenOf(login("dubu@test.com", PASSWORD).andExpect(status().isOk()));

        mvc.perform(get("/api/pastures/{id}", pastureId).header("X-Device-Token", again))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sheep[0].name").value("두부"));
    }

    @Test
    void 토큰_없이_로그아웃하면_401() throws Exception {
        expectError(mvc.perform(post("/api/auth/logout")), 401, "INVALID_DEVICE_TOKEN");
    }

    // ---------- helpers ----------

    private ResultActions signup(String sheepName, String email, String password) throws Exception {
        return mvc.perform(post("/api/auth/signup").contentType(MediaType.APPLICATION_JSON)
                .content("{\"sheepName\":\"" + sheepName + "\",\"email\":\"" + email
                        + "\",\"password\":\"" + password + "\"}"));
    }

    private ResultActions login(String email, String password) throws Exception {
        return mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"" + email + "\",\"password\":\"" + password + "\"}"));
    }

    private String tokenOf(ResultActions actions) throws Exception {
        return JsonPath.read(actions.andReturn().getResponse().getContentAsString(), "$.deviceToken");
    }

    private String createPasture(String token) throws Exception {
        MvcResult result = mvc.perform(post("/api/pastures").header("X-Device-Token", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"동기\",\"woolColor\":\"CREAM\",\"agreed\":true}"))
                .andExpect(status().isCreated()).andReturn();
        return JsonPath.read(result.getResponse().getContentAsString(), "$.pastureId");
    }

    private void expectError(ResultActions actions, int status, String code) throws Exception {
        actions.andExpect(status().is(status)).andExpect(jsonPath("$.code").value(code))
                .andExpect(jsonPath("$.message").isNotEmpty());
    }
}
