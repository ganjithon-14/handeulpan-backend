package com.ganjithon.handeulpan.support;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jayway.jsonpath.JsonPath;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

/** 테스트용 사용자 가입. 가입할 때 정한 이름이 양 이름이므로 테스트가 원하는 이름을 그대로 넘깁니다. */
public final class TestUsers {

    public static final String PASSWORD = "pass1234!";

    private TestUsers() {
    }

    /** 양 이름으로 가입하고 기기 토큰을 돌려줍니다. 이메일은 양 이름에서 만들어서 양 이름이 다르면 겹치지 않습니다. */
    public static String signup(MockMvc mvc, String sheepName) throws Exception {
        String body = mvc.perform(post("/api/auth/signup").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"sheepName\":\"" + sheepName + "\",\"email\":\"" + sheepName
                                + "@test.com\",\"password\":\"" + PASSWORD + "\"}"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return JsonPath.read(body, "$.deviceToken");
    }
}
