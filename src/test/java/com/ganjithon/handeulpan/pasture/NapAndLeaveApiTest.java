package com.ganjithon.handeulpan.pasture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.ganjithon.handeulpan.support.MutableClock;
import com.ganjithon.handeulpan.support.TestClockConfig;
import com.ganjithon.handeulpan.user.User;
import com.ganjithon.handeulpan.user.UserRepository;
import com.jayway.jsonpath.JsonPath;
import java.time.Duration;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

/** P1: 낮잠 모드와 들판 나가기. 사용자는 가입·토큰 발급 API와 무관하게 직접 만들어서 인증 방식이 바뀌어도 영향이 없습니다. */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(TestClockConfig.class)
@Transactional
class NapAndLeaveApiTest {

    @Autowired
    MockMvc mvc;

    @Autowired
    MutableClock clock;

    @Autowired
    UserRepository userRepository;

    @BeforeEach
    void resetClock() {
        clock.set(TestClockConfig.START);
    }

    // ---------- 낮잠 모드 ----------

    @Test
    void 낮잠_중에는_며칠이_지나도_멀어지지_않고_친구_배너에도_안_뜬다() throws Exception {
        String dubu = newUser();
        String friend = newUser();
        String pastureId = createPasture(dubu, "두부");
        long friendSheep = join(friend, pastureId, "콩이");

        mvc.perform(put("/api/sheep/{id}/nap", friendSheep).header("X-Device-Token", friend)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"days\": 7}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.napping").value(true))
                .andExpect(jsonPath("$.distance").value("CENTER"));

        clock.advance(Duration.ofDays(6));

        mvc.perform(get("/api/pastures/{id}", pastureId).header("X-Device-Token", dubu))
                .andExpect(jsonPath("$.sheep[1].name").value("콩이"))
                .andExpect(jsonPath("$.sheep[1].distance").value("CENTER"))
                .andExpect(jsonPath("$.sheep[1].napping").value(true))
                .andExpect(jsonPath("$.sheep[1].napUntil").doesNotExist());
        mvc.perform(get("/api/pastures").header("X-Device-Token", dubu))
                .andExpect(jsonPath("$[0].edgeFriendNames.length()").value(0));
        mvc.perform(get("/api/pastures/{id}/preview", pastureId))
                .andExpect(jsonPath("$.sheep[1].napping").value(true));
        // 내 홈에서도 내 낮잠 상태가 보임
        mvc.perform(get("/api/pastures").header("X-Device-Token", friend))
                .andExpect(jsonPath("$[0].napping").value(true));
    }

    @Test
    void 낮잠이_끝나면_그때부터_다시_멀어진다() throws Exception {
        String dubu = newUser();
        String friend = newUser();
        String pastureId = createPasture(dubu, "두부");
        long friendSheep = join(friend, pastureId, "콩이");

        mvc.perform(put("/api/sheep/{id}/nap", friendSheep).header("X-Device-Token", friend)
                .contentType(MediaType.APPLICATION_JSON).content("{\"days\": 3}")).andExpect(status().isOk());

        clock.advance(Duration.ofDays(3)); // 낮잠 끝
        assertFriendState(dubu, pastureId, "CENTER", false);
        clock.advance(Duration.ofDays(2)); // 끝난 뒤 2일
        assertFriendState(dubu, pastureId, "WALK", false);
        clock.advance(Duration.ofDays(2)); // 끝난 뒤 4일
        assertFriendState(dubu, pastureId, "EDGE", false);
    }

    @Test
    void 낮잠을_중간에_깨울_수_있고_깨운_뒤부터_다시_센다() throws Exception {
        String dubu = newUser();
        String friend = newUser();
        String pastureId = createPasture(dubu, "두부");
        long friendSheep = join(friend, pastureId, "콩이");

        mvc.perform(put("/api/sheep/{id}/nap", friendSheep).header("X-Device-Token", friend)
                .contentType(MediaType.APPLICATION_JSON).content("{\"days\": 7}")).andExpect(status().isOk());
        clock.advance(Duration.ofDays(1));
        mvc.perform(delete("/api/sheep/{id}/nap", friendSheep).header("X-Device-Token", friend))
                .andExpect(status().isNoContent());

        assertFriendState(dubu, pastureId, "CENTER", false);
        clock.advance(Duration.ofDays(2));
        assertFriendState(dubu, pastureId, "WALK", false);
    }

    @Test
    void 낮잠이_아닐_때_깨우기를_호출해도_멀어진_상태가_그대로다() throws Exception {
        String dubu = newUser();
        String friend = newUser();
        String pastureId = createPasture(dubu, "두부");
        long friendSheep = join(friend, pastureId, "콩이");

        clock.advance(Duration.ofDays(3));
        mvc.perform(delete("/api/sheep/{id}/nap", friendSheep).header("X-Device-Token", friend))
                .andExpect(status().isNoContent());
        assertFriendState(dubu, pastureId, "WALK", false);
    }

    @Test
    void 낮잠은_1에서_7일만_내_양만() throws Exception {
        String dubu = newUser();
        String friend = newUser();
        String pastureId = createPasture(dubu, "두부");
        long friendSheep = join(friend, pastureId, "콩이");

        for (String body : new String[] {"{\"days\": 0}", "{\"days\": 8}", "{}"}) {
            mvc.perform(put("/api/sheep/{id}/nap", friendSheep).header("X-Device-Token", friend)
                            .contentType(MediaType.APPLICATION_JSON).content(body))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"));
        }
        mvc.perform(put("/api/sheep/{id}/nap", friendSheep).header("X-Device-Token", dubu)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"days\": 3}"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("NOT_MY_SHEEP"));
        mvc.perform(delete("/api/sheep/{id}/nap", friendSheep).header("X-Device-Token", dubu))
                .andExpect(status().isForbidden());
    }

    // ---------- 들판 나가기 ----------

    @Test
    void 나가면_들판_조회와_목록과_미리보기에서_빠진다() throws Exception {
        String dubu = newUser();
        String friend = newUser();
        String pastureId = createPasture(dubu, "두부");
        long friendSheep = join(friend, pastureId, "콩이");

        mvc.perform(delete("/api/sheep/{id}", friendSheep).header("X-Device-Token", friend))
                .andExpect(status().isNoContent());

        mvc.perform(get("/api/pastures/{id}", pastureId).header("X-Device-Token", dubu))
                .andExpect(jsonPath("$.sheep.length()").value(1))
                .andExpect(jsonPath("$.sheep[0].name").value("두부"));
        mvc.perform(get("/api/pastures/{id}/preview", pastureId))
                .andExpect(jsonPath("$.sheep.length()").value(1));
        mvc.perform(get("/api/pastures").header("X-Device-Token", friend))
                .andExpect(jsonPath("$.length()").value(0));
        mvc.perform(get("/api/pastures/{id}", pastureId).header("X-Device-Token", friend))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("NOT_PASTURE_MEMBER"));
    }

    @Test
    void 나간_양은_쓰다듬기와_목도리_받기가_모두_없는_양으로_처리된다() throws Exception {
        String dubu = newUser();
        String friend = newUser();
        String pastureId = createPasture(dubu, "두부");
        long friendSheep = join(friend, pastureId, "콩이");
        mvc.perform(delete("/api/sheep/{id}", friendSheep).header("X-Device-Token", friend))
                .andExpect(status().isNoContent());

        mvc.perform(post("/api/sheep/{id}/pat", friendSheep).header("X-Device-Token", friend))
                .andExpect(status().isNotFound()).andExpect(jsonPath("$.code").value("SHEEP_NOT_FOUND"));
        // BE-B 목도리 API도 SheepFinder를 거치므로 나간 양에게는 보낼 수 없음
        mvc.perform(post("/api/sheep/{id}/scarves", friendSheep).header("X-Device-Token", dubu))
                .andExpect(status().isNotFound()).andExpect(jsonPath("$.code").value("SHEEP_NOT_FOUND"));
    }

    @Test
    void 나가면_자리가_비어서_가득_찬_들판에도_새로_합류할_수_있다() throws Exception {
        String owner = newUser();
        String pastureId = createPasture(owner, "양1");
        long leaving = 0;
        for (int i = 2; i <= 6; i++) {
            String token = newUser();
            long id = join(token, pastureId, "양" + i);
            if (i == 6) {
                leaving = id;
                mvc.perform(delete("/api/sheep/{id}", leaving).header("X-Device-Token", token))
                        .andExpect(status().isNoContent());
            }
        }
        mvc.perform(get("/api/pastures/{id}/preview", pastureId)).andExpect(jsonPath("$.full").value(false));
        join(newUser(), pastureId, "새양");
        mvc.perform(get("/api/pastures/{id}/preview", pastureId)).andExpect(jsonPath("$.full").value(true));
    }

    @Test
    void 다시_합류하면_같은_양이_가운데에서_새로_시작한다() throws Exception {
        String dubu = newUser();
        String friend = newUser();
        String pastureId = createPasture(dubu, "두부");
        long first = join(friend, pastureId, "콩이");
        mvc.perform(put("/api/sheep/{id}/nap", first).header("X-Device-Token", friend)
                .contentType(MediaType.APPLICATION_JSON).content("{\"days\": 7}")).andExpect(status().isOk());
        clock.advance(Duration.ofDays(10));
        mvc.perform(delete("/api/sheep/{id}", first).header("X-Device-Token", friend))
                .andExpect(status().isNoContent());

        long again = join(friend, pastureId, "콩콩");
        assertThat(again).isEqualTo(first);

        mvc.perform(get("/api/pastures/{id}", pastureId).header("X-Device-Token", friend))
                .andExpect(jsonPath("$.sheep.length()").value(2))
                .andExpect(jsonPath("$.sheep[1].name").value("콩콩"))
                .andExpect(jsonPath("$.sheep[1].distance").value("CENTER"))
                .andExpect(jsonPath("$.sheep[1].napping").value(false))
                .andExpect(jsonPath("$.mySheepId").value(first));
    }

    @Test
    void 이미_들어와_있으면_다시_합류할_수_없고_남의_양은_내보낼_수_없다() throws Exception {
        String dubu = newUser();
        String friend = newUser();
        String pastureId = createPasture(dubu, "두부");
        long friendSheep = join(friend, pastureId, "콩이");

        mvc.perform(post("/api/pastures/{id}/sheep", pastureId).header("X-Device-Token", friend)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"sheepName\":\"또\",\"woolColor\":\"MINT\",\"agreed\":true}"))
                .andExpect(status().isConflict()).andExpect(jsonPath("$.code").value("ALREADY_JOINED"));
        mvc.perform(delete("/api/sheep/{id}", friendSheep).header("X-Device-Token", dubu))
                .andExpect(status().isForbidden()).andExpect(jsonPath("$.code").value("NOT_MY_SHEEP"));
        // 이미 나간 양을 또 내보내려 해도 없는 양
        mvc.perform(delete("/api/sheep/{id}", friendSheep).header("X-Device-Token", friend))
                .andExpect(status().isNoContent());
        mvc.perform(delete("/api/sheep/{id}", friendSheep).header("X-Device-Token", friend))
                .andExpect(status().isNotFound());
    }

    // ---------- helpers ----------

    /** 가입·토큰 발급 API와 무관하게 사용자를 직접 만듭니다. */
    private String newUser() {
        String token = UUID.randomUUID().toString();
        userRepository.save(new User(token));
        return token;
    }

    private String createPasture(String token, String sheepName) throws Exception {
        MvcResult result = mvc.perform(post("/api/pastures").header("X-Device-Token", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"대학 동기\",\"sheepName\":\"" + sheepName
                                + "\",\"woolColor\":\"CREAM\",\"agreed\":true}"))
                .andExpect(status().isCreated()).andReturn();
        return JsonPath.read(result.getResponse().getContentAsString(), "$.pastureId");
    }

    private long join(String token, String pastureId, String sheepName) throws Exception {
        ResultActions joined = mvc.perform(post("/api/pastures/{id}/sheep", pastureId)
                .header("X-Device-Token", token).contentType(MediaType.APPLICATION_JSON)
                .content("{\"sheepName\":\"" + sheepName + "\",\"woolColor\":\"MINT\",\"agreed\":true}"));
        Number id = JsonPath.read(joined.andExpect(status().isCreated()).andReturn()
                .getResponse().getContentAsString(), "$.sheepId");
        return id.longValue();
    }

    /** 두부가 보는 콩이(두 번째 양)의 상태 */
    private void assertFriendState(String viewer, String pastureId, String distance, boolean napping) throws Exception {
        mvc.perform(get("/api/pastures/{id}", pastureId).header("X-Device-Token", viewer))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sheep[1].distance").value(distance))
                .andExpect(jsonPath("$.sheep[1].napping").value(napping));
    }
}
