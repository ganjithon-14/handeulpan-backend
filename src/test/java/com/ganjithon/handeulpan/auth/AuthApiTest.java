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

/** 아이디·비밀번호 가입 / 로그인 / 로그아웃 (기기 토큰 UUID 방식 유지) */
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
    void 토큰_없이_가입하면_새_사용자와_토큰을_받고_그_토큰이_통한다() throws Exception {
        String token = tokenOf(signup(null, "dubu01", PASSWORD).andExpect(status().isCreated()));

        mvc.perform(get("/api/pastures").header("X-Device-Token", token)).andExpect(status().isOk());
    }

    @Test
    void 기기_토큰_사용자가_가입하면_토큰과_양이_그대로_유지된다() throws Exception {
        String token = issueToken();
        String pastureId = createPasture(token, "두부");

        String afterSignup = tokenOf(signup(token, "dubu01", PASSWORD).andExpect(status().isCreated()));

        assertThat(afterSignup).isEqualTo(token);
        mvc.perform(get("/api/pastures/{id}", pastureId).header("X-Device-Token", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sheep[0].name").value("두부"));
    }

    @Test
    void 모르는_토큰으로_가입하면_새_사용자로_가입된다() throws Exception {
        String token = tokenOf(signup("no-such-token", "dubu01", PASSWORD).andExpect(status().isCreated()));

        assertThat(token).isNotEqualTo("no-such-token");
    }

    @Test
    void 이미_계정이_있는_사용자가_또_가입하면_409() throws Exception {
        String token = tokenOf(signup(null, "dubu01", PASSWORD));

        expectError(signup(token, "other01", PASSWORD), 409, "ALREADY_HAS_ACCOUNT");
    }

    @Test
    void 아이디는_대소문자_구분_없이_중복이면_409() throws Exception {
        signup(null, "Dubu01", PASSWORD).andExpect(status().isCreated());

        expectError(signup(null, "dubu01", PASSWORD), 409, "DUPLICATE_USERNAME");
        expectError(signup(null, "DUBU01", PASSWORD), 409, "DUPLICATE_USERNAME");
    }

    @Test
    void 아이디와_비밀번호_형식이_틀리면_400() throws Exception {
        expectError(signup(null, "abc", PASSWORD), 400, "VALIDATION_FAILED");              // 4자 미만
        expectError(signup(null, "a".repeat(21), PASSWORD), 400, "VALIDATION_FAILED");     // 20자 초과
        expectError(signup(null, "두부두부두부", PASSWORD), 400, "VALIDATION_FAILED");        // 한글
        expectError(signup(null, "dubu 01", PASSWORD), 400, "VALIDATION_FAILED");          // 공백
        expectError(signup(null, "dubu01", "short1!"), 400, "VALIDATION_FAILED");          // 8자 미만
        expectError(signup(null, "dubu01", "pass word1"), 400, "VALIDATION_FAILED");       // 공백
        expectError(signup(null, "dubu01", "비밀번호비밀번호비밀"), 400, "VALIDATION_FAILED"); // 한글
        expectError(mvc.perform(post("/api/auth/signup").contentType(MediaType.APPLICATION_JSON).content("{}")),
                400, "VALIDATION_FAILED");
    }

    @Test
    void 비밀번호는_해시로_저장되고_응답에_나오지_않는다(@Autowired AccountRepository accounts) throws Exception {
        String body = signup(null, "dubu01", PASSWORD).andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        assertThat(body).doesNotContain(PASSWORD);
        Account account = accounts.findByUsername("dubu01").orElseThrow();
        assertThat(account.getPasswordHash()).isNotEqualTo(PASSWORD).startsWith("$2");
    }

    // ---------- 로그인 ----------

    @Test
    void 로그인하면_새_토큰을_받고_같은_양이_보인다() throws Exception {
        String oldToken = issueToken();
        String pastureId = createPasture(oldToken, "두부");
        signup(oldToken, "dubu01", PASSWORD).andExpect(status().isCreated());

        String newToken = tokenOf(login("dubu01", PASSWORD).andExpect(status().isOk()));

        assertThat(newToken).isNotEqualTo(oldToken);
        mvc.perform(get("/api/pastures/{id}", pastureId).header("X-Device-Token", newToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sheep[0].name").value("두부"));
        // 이전 토큰은 더 이상 통하지 않는다 (다른 기기에서 로그인한 것과 같다)
        expectError(mvc.perform(get("/api/pastures").header("X-Device-Token", oldToken)),
                401, "INVALID_DEVICE_TOKEN");
    }

    @Test
    void 로그인_아이디는_대소문자를_가리지_않는다() throws Exception {
        signup(null, "Dubu01", PASSWORD).andExpect(status().isCreated());

        login("DUBU01", PASSWORD).andExpect(status().isOk());
    }

    @Test
    void 틀린_비밀번호와_없는_아이디는_같은_401로_답한다() throws Exception {
        signup(null, "dubu01", PASSWORD).andExpect(status().isCreated());

        expectError(login("dubu01", "wrong-password1"), 401, "INVALID_CREDENTIALS");
        expectError(login("nobody01", PASSWORD), 401, "INVALID_CREDENTIALS");
    }

    @Test
    void 로그인_본문이_비었으면_400() throws Exception {
        expectError(mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON).content("{}")),
                400, "VALIDATION_FAILED");
    }

    // ---------- 로그아웃 ----------

    @Test
    void 로그아웃하면_204이고_그_토큰은_쓸_수_없다() throws Exception {
        String token = tokenOf(signup(null, "dubu01", PASSWORD));

        mvc.perform(post("/api/auth/logout").header("X-Device-Token", token)).andExpect(status().isNoContent());

        expectError(mvc.perform(get("/api/pastures").header("X-Device-Token", token)),
                401, "INVALID_DEVICE_TOKEN");
    }

    @Test
    void 로그아웃_뒤_다시_로그인하면_데이터가_그대로_있다() throws Exception {
        String token = issueToken();
        String pastureId = createPasture(token, "두부");
        signup(token, "dubu01", PASSWORD).andExpect(status().isCreated());

        mvc.perform(post("/api/auth/logout").header("X-Device-Token", token)).andExpect(status().isNoContent());
        String again = tokenOf(login("dubu01", PASSWORD).andExpect(status().isOk()));

        mvc.perform(get("/api/pastures/{id}", pastureId).header("X-Device-Token", again))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sheep[0].name").value("두부"));
    }

    @Test
    void 토큰_없이_로그아웃하면_401() throws Exception {
        expectError(mvc.perform(post("/api/auth/logout")), 401, "INVALID_DEVICE_TOKEN");
    }

    // ---------- helpers ----------

    private ResultActions signup(String token, String username, String password) throws Exception {
        var request = post("/api/auth/signup").contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"" + username + "\",\"password\":\"" + password + "\"}");
        if (token != null) {
            request.header("X-Device-Token", token);
        }
        return mvc.perform(request);
    }

    private ResultActions login(String username, String password) throws Exception {
        return mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"" + username + "\",\"password\":\"" + password + "\"}"));
    }

    private String tokenOf(ResultActions actions) throws Exception {
        return JsonPath.read(actions.andReturn().getResponse().getContentAsString(), "$.deviceToken");
    }

    private String issueToken() throws Exception {
        MvcResult result = mvc.perform(post("/api/users")).andExpect(status().isCreated()).andReturn();
        return JsonPath.read(result.getResponse().getContentAsString(), "$.deviceToken");
    }

    private String createPasture(String token, String sheepName) throws Exception {
        MvcResult result = mvc.perform(post("/api/pastures").header("X-Device-Token", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"동기\",\"sheepName\":\"" + sheepName
                                + "\",\"woolColor\":\"CREAM\",\"agreed\":true}"))
                .andExpect(status().isCreated()).andReturn();
        return JsonPath.read(result.getResponse().getContentAsString(), "$.pastureId");
    }

    private void expectError(ResultActions actions, int status, String code) throws Exception {
        actions.andExpect(status().is(status)).andExpect(jsonPath("$.code").value(code))
                .andExpect(jsonPath("$.message").isNotEmpty());
    }
}
