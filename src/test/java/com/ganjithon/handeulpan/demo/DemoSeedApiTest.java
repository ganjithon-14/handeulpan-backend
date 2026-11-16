package com.ganjithon.handeulpan.demo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.ganjithon.handeulpan.support.MutableClock;
import com.ganjithon.handeulpan.support.TestClockConfig;
import com.ganjithon.handeulpan.support.TestUsers;
import com.jayway.jsonpath.JsonPath;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

/** 시연용 시드 데이터: 만들기, 다시 만들기, 지우기. */
@SuppressWarnings("unchecked")
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(TestClockConfig.class)
@TestPropertySource(properties = "handeulpan.demo.key=test-demo-key")
@Transactional
class DemoSeedApiTest {

    static final String KEY = "test-demo-key";
    static final String PASSWORD = "demo1234!";

    @Autowired
    MockMvc mvc;

    @Autowired
    MutableClock clock;

    @BeforeEach
    void resetClock() {
        clock.set(TestClockConfig.START);
    }

    @Test
    void 데모_키가_없거나_틀리면_403() throws Exception {
        expectError(mvc.perform(post("/api/demo/seed")), 403, "INVALID_DEMO_KEY");
        expectError(mvc.perform(post("/api/demo/seed").header("X-Demo-Key", "wrong")), 403, "INVALID_DEMO_KEY");
        expectError(mvc.perform(delete("/api/demo/seed")), 403, "INVALID_DEMO_KEY");
    }

    @Test
    void 시드를_부르면_시연_상태의_들판이_만들어진다() throws Exception {
        String body = seed().andExpect(status().isCreated())
                .andExpect(jsonPath("$.password").value(PASSWORD))
                .andExpect(jsonPath("$.members.length()").value(5))
                .andExpect(jsonPath("$.members[?(@.me == true)].email").value("doobu@demo.test"))
                .andReturn().getResponse().getContentAsString();
        String pastureId = JsonPath.read(body, "$.pastureId");

        String dubu = login("doobu@demo.test");
        mvc.perform(get("/api/pastures/{id}", pastureId).header("X-Device-Token", dubu))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("한 들판 데모"))
                .andExpect(jsonPath("$.sheep.length()").value(5))
                .andExpect(jsonPath("$.sheep[?(@.name == '두부')].mine").value(true))
                .andExpect(jsonPath("$.sheep[?(@.name == '두부')].distance").value("CENTER"))
                .andExpect(jsonPath("$.sheep[?(@.name == '콩이')].distance").value("CENTER"))
                .andExpect(jsonPath("$.sheep[?(@.name == '콩이')].bubble.text").value("시험 끝났다!"))
                .andExpect(jsonPath("$.sheep[?(@.name == '보리')].distance").value("WALK"))
                .andExpect(jsonPath("$.sheep[?(@.name == '별이')].napping").value(true))
                .andExpect(jsonPath("$.sheep[?(@.name == '별이')].distance").value("CENTER"))
                // 두부는 친구 세 명에게 목도리를 받았고, 마지막에 받은 콩이(MINT) 것을 두르고 있다
                .andExpect(jsonPath("$.sheep[?(@.name == '두부')].wornScarf").value("MINT"))
                .andExpect(jsonPath("$.myScarves.length()").value(3));
        // 홈: 배너에 뜰 친구는 아직 없다. (양 5마리라 한 자리가 비어 있다)
        mvc.perform(get("/api/pastures").header("X-Device-Token", dubu))
                .andExpect(jsonPath("$[0].edgeFriendNames.length()").value(0));
    }

    @Test
    void 되감기로_콩이를_가장자리로_보내면_두부_홈에_배너가_뜬다() throws Exception {
        String body = seed().andReturn().getResponse().getContentAsString();
        Number kongSheep = ((List<Number>) JsonPath.read(body, "$.members[?(@.sheepName == '콩이')].sheepId")).get(0);

        mvc.perform(post("/api/demo/sheep/{id}/rewind", kongSheep.longValue())
                        .header("X-Demo-Key", KEY).contentType(MediaType.APPLICATION_JSON).content("{\"days\": 4}"))
                .andExpect(jsonPath("$.distance").value("EDGE"));

        mvc.perform(get("/api/pastures").header("X-Device-Token", login("doobu@demo.test")))
                .andExpect(jsonPath("$[0].edgeFriendNames[0]").value("콩이"));
    }

    @Test
    void 다시_부르면_같은_상태로_초기화되고_데모_데이터는_하나뿐이다() throws Exception {
        String first = JsonPath.read(seed().andReturn().getResponse().getContentAsString(), "$.pastureId");
        // 첫 번째 시드 상태를 흐트러뜨린다: 콩이를 되감고, 두부가 쓰다듬은 뒤 새 친구가 들어옴
        Number kongSheep = ((List<Number>) JsonPath.read(
                mvc.perform(get("/api/pastures/{id}", first).header("X-Device-Token", login("doobu@demo.test")))
                        .andReturn().getResponse().getContentAsString(), "$.sheep[?(@.name == '콩이')].id")).get(0);
        mvc.perform(post("/api/demo/sheep/{id}/rewind", kongSheep.longValue())
                .header("X-Demo-Key", KEY).contentType(MediaType.APPLICATION_JSON).content("{\"days\": 5}"));
        String stranger = TestUsers.signupNew(mvc);
        mvc.perform(post("/api/pastures/{id}/sheep", first).header("X-Device-Token", stranger)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"sheepName\":\"손님\",\"woolColor\":\"PINK\",\"agreed\":true}"))
                .andExpect(status().isCreated());

        String second = JsonPath.read(seed().andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString(), "$.pastureId");

        assertThat(second).isNotEqualTo(first);
        String dubu = login("doobu@demo.test");
        mvc.perform(get("/api/pastures").header("X-Device-Token", dubu))
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].pastureId").value(second))
                .andExpect(jsonPath("$[0].edgeFriendNames.length()").value(0));
        // 이전 들판은 사라졌다
        mvc.perform(get("/api/pastures/{id}/preview", first)).andExpect(status().isNotFound());
        // 데모와 상관없는 가입자 계정은 그대로 남는다 (들판의 양만 사라짐)
        mvc.perform(get("/api/pastures").header("X-Device-Token", stranger))
                .andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void 지우면_데모_계정과_들판이_모두_없어진다() throws Exception {
        String pastureId = JsonPath.read(seed().andReturn().getResponse().getContentAsString(), "$.pastureId");

        mvc.perform(delete("/api/demo/seed").header("X-Demo-Key", KEY)).andExpect(status().isNoContent());

        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"doobu@demo.test\",\"password\":\"" + PASSWORD + "\"}"))
                .andExpect(status().isUnauthorized());
        mvc.perform(get("/api/pastures/{id}/preview", pastureId)).andExpect(status().isNotFound());
        // 데모 데이터가 없을 때 또 지워도 에러가 아니다
        mvc.perform(delete("/api/demo/seed").header("X-Demo-Key", KEY)).andExpect(status().isNoContent());
    }

    // ---------- helpers ----------

    private ResultActions seed() throws Exception {
        return mvc.perform(post("/api/demo/seed").header("X-Demo-Key", KEY));
    }

    private String login(String email) throws Exception {
        String body = mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"" + email + "\",\"password\":\"" + PASSWORD + "\"}"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        return JsonPath.read(body, "$.deviceToken");
    }

    private void expectError(ResultActions actions, int status, String code) throws Exception {
        actions.andExpect(status().is(status)).andExpect(jsonPath("$.code").value(code));
    }
}
