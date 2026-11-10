package com.ganjithon.handeulpan.pasture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.ganjithon.handeulpan.support.MutableClock;
import com.ganjithon.handeulpan.support.TestClockConfig;
import com.jayway.jsonpath.JsonPath;
import java.time.Duration;
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
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

/** 11/10 핵심 루프: 링크로 합류 → 쓰다듬기 → 데모 되감기로 양이 가장자리까지 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(TestClockConfig.class)
@TestPropertySource(properties = "handeulpan.demo.key=test-demo-key")
@Transactional
class CoreLoopApiTest {

    @Autowired
    MockMvc mvc;

    @Autowired
    MutableClock clock;

    @BeforeEach
    void resetClock() {
        clock.set(TestClockConfig.START);
    }

    // ---------- 핵심 루프 ----------

    @Test
    void 들판_만들고_링크로_합류해서_같이_보인다() throws Exception {
        String dubu = issueToken();
        String friend = issueToken();
        String pastureId = createPasture(dubu, "두부");

        mvc.perform(get("/api/pastures/{id}/preview", pastureId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.maxSheep").value(6))
                .andExpect(jsonPath("$.full").value(false))
                .andExpect(jsonPath("$.sheep.length()").value(1))
                .andExpect(jsonPath("$.sheep[0].name").value("두부"))
                .andExpect(jsonPath("$.sheep[0].distance").value("CENTER"))
                .andExpect(jsonPath("$.sheep[0].id").doesNotExist());

        join(friend, pastureId, "콩이").andExpect(status().isCreated())
                .andExpect(jsonPath("$.pastureId").value(pastureId))
                .andExpect(jsonPath("$.sheepId").isNumber());

        mvc.perform(get("/api/pastures/{id}", pastureId).header("X-Device-Token", friend))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sheep.length()").value(2))
                .andExpect(jsonPath("$.sheep[0].name").value("두부"))
                .andExpect(jsonPath("$.sheep[0].mine").value(false))
                .andExpect(jsonPath("$.sheep[0].pattedToday").doesNotExist())
                .andExpect(jsonPath("$.sheep[1].name").value("콩이"))
                .andExpect(jsonPath("$.sheep[1].mine").value(true))
                .andExpect(jsonPath("$.sheep[1].pattedToday").value(true))
                .andExpect(jsonPath("$.sheep[0].lastPattedAt").doesNotExist());
    }

    @Test
    void 쓰다듬지_않으면_멀어지고_쓰다듬으면_가운데로_돌아온다() throws Exception {
        String dubu = issueToken();
        String friend = issueToken();
        String pastureId = createPasture(dubu, "두부");
        long friendSheep = sheepIdOf(join(friend, pastureId, "콩이"));

        clock.advance(Duration.ofDays(2));
        assertDistance(friend, pastureId, 0, "WALK");

        clock.advance(Duration.ofDays(2));
        assertDistance(friend, pastureId, 0, "EDGE");

        // 홈 배너: 가장자리에 간 친구 이름
        mvc.perform(get("/api/pastures").header("X-Device-Token", friend))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].pastureId").value(pastureId))
                .andExpect(jsonPath("$[0].edgeFriendNames[0]").value("두부"));

        String dubuSheep = JsonPath.read(mvc.perform(get("/api/pastures/{id}", pastureId)
                .header("X-Device-Token", dubu)).andReturn().getResponse().getContentAsString(), "$.mySheepId").toString();
        mvc.perform(post("/api/sheep/{id}/pat", dubuSheep).header("X-Device-Token", dubu))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.distance").value("CENTER"))
                .andExpect(jsonPath("$.pattedToday").value(true));
        assertDistance(friend, pastureId, 0, "CENTER");

        // 남의 양은 쓰다듬을 수 없음
        expectError(mvc.perform(post("/api/sheep/{id}/pat", friendSheep).header("X-Device-Token", dubu)),
                403, "NOT_MY_SHEEP");
    }

    @Test
    void 데모_되감기로_양을_바로_가장자리로_보낸다() throws Exception {
        String dubu = issueToken();
        String friend = issueToken();
        String pastureId = createPasture(dubu, "두부");
        long friendSheep = sheepIdOf(join(friend, pastureId, "콩이"));

        mvc.perform(post("/api/demo/sheep/{id}/rewind", friendSheep)
                        .header("X-Demo-Key", "test-demo-key")
                        .contentType(MediaType.APPLICATION_JSON).content("{\"days\": 4}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.distance").value("EDGE"));
        assertDistance(dubu, pastureId, 1, "EDGE");

        expectError(mvc.perform(post("/api/demo/sheep/{id}/rewind", friendSheep)
                .header("X-Demo-Key", "wrong")
                .contentType(MediaType.APPLICATION_JSON).content("{\"days\": 4}")), 403, "INVALID_DEMO_KEY");
    }

    // ---------- 규칙 ----------

    @Test
    void 들판은_6마리까지_한_사람은_한_번만() throws Exception {
        String owner = issueToken();
        String pastureId = createPasture(owner, "양1");
        for (int i = 2; i <= 6; i++) {
            join(issueToken(), pastureId, "양" + i).andExpect(status().isCreated());
        }
        mvc.perform(get("/api/pastures/{id}/preview", pastureId)).andExpect(jsonPath("$.full").value(true));
        expectError(join(issueToken(), pastureId, "양7"), 409, "PASTURE_FULL");
        expectError(join(owner, pastureId, "또양"), 409, "ALREADY_JOINED");
    }

    @Test
    void 동의하지_않으면_합류할_수_없다() throws Exception {
        String pastureId = createPasture(issueToken(), "두부");
        mvc.perform(post("/api/pastures/{id}/sheep", pastureId)
                        .header("X-Device-Token", issueToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"sheepName\":\"콩이\",\"woolColor\":\"MINT\",\"agreed\":false}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void 토큰이_없거나_모르는_토큰이면_401() throws Exception {
        expectError(mvc.perform(get("/api/pastures")), 401, "INVALID_DEVICE_TOKEN");
        expectError(mvc.perform(get("/api/pastures").header("X-Device-Token", "nope")), 401, "INVALID_DEVICE_TOKEN");
    }

    @Test
    void 멤버가_아니면_들판을_조회할_수_없고_없는_들판은_404() throws Exception {
        String pastureId = createPasture(issueToken(), "두부");
        expectError(mvc.perform(get("/api/pastures/{id}", pastureId).header("X-Device-Token", issueToken())),
                403, "NOT_PASTURE_MEMBER");
        expectError(mvc.perform(get("/api/pastures/{id}/preview", "no-such-pasture")), 404, "PASTURE_NOT_FOUND");
    }

    // ---------- helpers ----------

    private String issueToken() throws Exception {
        MvcResult result = mvc.perform(post("/api/users")).andExpect(status().isCreated()).andReturn();
        return JsonPath.read(result.getResponse().getContentAsString(), "$.deviceToken");
    }

    private String createPasture(String token, String sheepName) throws Exception {
        MvcResult result = mvc.perform(post("/api/pastures")
                        .header("X-Device-Token", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"대학 동기\",\"sheepName\":\"" + sheepName
                                + "\",\"woolColor\":\"CREAM\",\"agreed\":true}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.sheepId").isNumber())
                .andReturn();
        String id = JsonPath.read(result.getResponse().getContentAsString(), "$.pastureId");
        assertThat(id).hasSize(22);
        return id;
    }

    private ResultActions join(String token, String pastureId, String sheepName) throws Exception {
        return mvc.perform(post("/api/pastures/{id}/sheep", pastureId)
                .header("X-Device-Token", token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"sheepName\":\"" + sheepName + "\",\"woolColor\":\"MINT\",\"agreed\":true}"));
    }

    private long sheepIdOf(ResultActions joined) throws Exception {
        Number id = JsonPath.read(joined.andExpect(status().isCreated()).andReturn()
                .getResponse().getContentAsString(), "$.sheepId");
        return id.longValue();
    }

    private void assertDistance(String viewer, String pastureId, int index, String distance) throws Exception {
        mvc.perform(get("/api/pastures/{id}", pastureId).header("X-Device-Token", viewer))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sheep[" + index + "].distance").value(distance));
    }

    private void expectError(ResultActions actions, int status, String code) throws Exception {
        MvcResult result = actions.andExpect(status().is(status)).andReturn();
        assertThat(result.getResolvedException()).isInstanceOf(ResponseStatusException.class);
        assertThat(((ResponseStatusException) result.getResolvedException()).getReason()).isEqualTo(code);
    }
}
