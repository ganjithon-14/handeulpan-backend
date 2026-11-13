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

    /** 겹치지 않는 닉네임으로 가입하고 토큰을 돌려줍니다. 닉네임이 필요 없는 테스트용입니다. */
    public static String signupNew(MockMvc mvc) throws Exception {
        return signup(mvc, "유저" + SEQ.incrementAndGet());
    }

    /** 닉네임으로 가입하고 기기 토큰을 돌려줍니다. 이메일은 닉네임에서 만들어서 닉네임이 다르면 겹치지 않습니다. */
    public static String signup(MockMvc mvc, String nickname) throws Exception {
        String body = mvc.perform(post("/api/auth/signup").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nickname\":\"" + nickname + "\",\"email\":\"" + nickname
                                + "@test.com\",\"password\":\"" + PASSWORD + "\"}"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return JsonPath.read(body, "$.deviceToken");
    }
}
