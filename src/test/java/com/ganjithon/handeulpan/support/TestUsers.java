package com.ganjithon.handeulpan.support;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jayway.jsonpath.JsonPath;
import java.util.concurrent.atomic.AtomicInteger;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

/** 테스트용 사용자 가입. */
public final class TestUsers {

    public static final String PASSWORD = "pass1234!";

    private static final AtomicInteger SEQ = new AtomicInteger();

    private TestUsers() {
    }

    /** 겹치지 않는 이메일로 가입하고 기기 토큰을 돌려줍니다. */
    public static String signupNew(MockMvc mvc) throws Exception {
        String body = mvc.perform(post("/api/auth/signup").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"user" + SEQ.incrementAndGet()
                                + "@test.com\",\"password\":\"" + PASSWORD + "\"}"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return JsonPath.read(body, "$.deviceToken");
    }
}
