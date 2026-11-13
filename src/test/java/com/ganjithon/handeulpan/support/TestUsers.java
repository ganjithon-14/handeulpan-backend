package com.ganjithon.handeulpan.support;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jayway.jsonpath.JsonPath;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

/** 테스트용 사용자 가입. 닉네임이 양 이름이 되므로 테스트가 원하는 이름을 그대로 넘깁니다. */
public final class TestUsers {

    public static final String PASSWORD = "pass1234!";

    private TestUsers() {
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
