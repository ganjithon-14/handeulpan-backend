package com.ganjithon.handeulpan.bubble;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
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
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

/** F4 말풍선: 올리기·교체·지우기·위험 키워드·24시간 만료·들판 조회 반영 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(TestClockConfig.class)
@Transactional
class BubbleApiTest {

    @Autowired
    MockMvc mvc;

    @Autowired
    MutableClock clock;

    String owner;
    String friend;
    String pastureId;
    long ownerSheep;
    long friendSheep;

    @BeforeEach
    void setUp() throws Exception {
        clock.set(TestClockConfig.START);
        owner = issueToken();
        friend = issueToken();
        MvcResult created = mvc.perform(post("/api/pastures").header("X-Device-Token", owner)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"동기\",\"sheepName\":\"두부\",\"woolColor\":\"CREAM\",\"agreed\":true}"))
                .andExpect(status().isCreated()).andReturn();
        pastureId = JsonPath.read(created.getResponse().getContentAsString(), "$.pastureId");
        ownerSheep = ((Number) JsonPath.read(created.getResponse().getContentAsString(), "$.sheepId")).longValue();
        MvcResult joined = mvc.perform(post("/api/pastures/{id}/sheep", pastureId).header("X-Device-Token", friend)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"sheepName\":\"콩이\",\"woolColor\":\"MINT\",\"agreed\":true}"))
                .andExpect(status().isCreated()).andReturn();
        friendSheep = ((Number) JsonPath.read(joined.getResponse().getContentAsString(), "$.sheepId")).longValue();
    }

    // ---------- 올리기 ----------

    @Test
    void 일반_글은_바로_게시된다() throws Exception {
        putBubble(owner, ownerSheep, "오늘 입사 100일", false)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.posted").value(true))
                .andExpect(jsonPath("$.flagged").value(false));
    }

    @Test
    void 게시한_말풍선은_들판_조회에서_친구에게도_보인다() throws Exception {
        putBubble(owner, ownerSheep, "오늘 입사 100일", false).andExpect(status().isOk());

        mvc.perform(get("/api/pastures/{id}", pastureId).header("X-Device-Token", friend))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sheep[0].bubble.text").value("오늘 입사 100일"))
                .andExpect(jsonPath("$.sheep[0].bubble.flagged").value(false))
                .andExpect(jsonPath("$.sheep[0].bubble.createdAt").isNotEmpty())
                // 말풍선이 없는 양은 bubble이 null
                .andExpect(jsonPath("$.sheep[1].bubble").value((Object) null));
    }

    @Test
    void 새로_올리면_이전_말풍선을_교체한다() throws Exception {
        putBubble(owner, ownerSheep, "첫 번째", false).andExpect(status().isOk());
        clock.advance(Duration.ofHours(2));
        putBubble(owner, ownerSheep, "두 번째", false).andExpect(status().isOk());

        mvc.perform(get("/api/pastures/{id}", pastureId).header("X-Device-Token", owner))
                .andExpect(jsonPath("$.sheep[0].bubble.text").value("두 번째"));
    }

    // ---------- 위험 키워드 ----------

    @Test
    void 위험_키워드는_확인_없이는_게시하지_않는다() throws Exception {
        putBubble(owner, ownerSheep, "요즘 너무 죽고 싶다", false)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.posted").value(false))
                .andExpect(jsonPath("$.flagged").value(true));

        mvc.perform(get("/api/pastures/{id}", pastureId).header("X-Device-Token", owner))
                .andExpect(jsonPath("$.sheep[0].bubble").value((Object) null));
    }

    @Test
    void 위험_키워드도_확인하면_flagged로_게시된다() throws Exception {
        putBubble(owner, ownerSheep, "요즘 너무 죽고 싶다", true)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.posted").value(true))
                .andExpect(jsonPath("$.flagged").value(true));

        mvc.perform(get("/api/pastures/{id}", pastureId).header("X-Device-Token", friend))
                .andExpect(jsonPath("$.sheep[0].bubble.flagged").value(true));
    }

    @Test
    void 위험_키워드는_공백이_달라도_잡힌다() throws Exception {
        putBubble(owner, ownerSheep, "죽고싶어", false)
                .andExpect(jsonPath("$.flagged").value(true))
                .andExpect(jsonPath("$.posted").value(false));
    }

    @Test
    void 위험_키워드가_없는데_confirm만_true여도_flagged는_false() throws Exception {
        putBubble(owner, ownerSheep, "오늘 날씨 좋다", true)
                .andExpect(jsonPath("$.posted").value(true))
                .andExpect(jsonPath("$.flagged").value(false));
    }

    // ---------- 글자 수 ----------

    @Test
    void 비었거나_공백뿐이면_400() throws Exception {
        expectError(putBubble(owner, ownerSheep, "", false), 400, "BUBBLE_TEXT_INVALID");
        expectError(putBubble(owner, ownerSheep, "    ", false), 400, "BUBBLE_TEXT_INVALID");
        expectError(mvc.perform(put("/api/sheep/{id}/bubble", ownerSheep).header("X-Device-Token", owner)
                .contentType(MediaType.APPLICATION_JSON).content("{}")), 400, "BUBBLE_TEXT_INVALID");
    }

    @Test
    void 스무_자까지_되고_스물한_자는_400() throws Exception {
        putBubble(owner, ownerSheep, "가".repeat(20), false).andExpect(status().isOk());
        expectError(putBubble(owner, ownerSheep, "가".repeat(21), false), 400, "BUBBLE_TEXT_INVALID");
    }

    @Test
    void 이모지는_한_글자로_센다() throws Exception {
        // 😀 하나는 UTF-16으로 2칸이지만 1글자
        putBubble(owner, ownerSheep, "😀".repeat(20), false).andExpect(status().isOk());
        expectError(putBubble(owner, ownerSheep, "😀".repeat(21), false), 400, "BUBBLE_TEXT_INVALID");
    }

    @Test
    void 앞뒤_공백은_떼고_저장한다() throws Exception {
        putBubble(owner, ownerSheep, "  안녕  ", false).andExpect(status().isOk());

        mvc.perform(get("/api/pastures/{id}", pastureId).header("X-Device-Token", owner))
                .andExpect(jsonPath("$.sheep[0].bubble.text").value("안녕"));
    }

    // ---------- 권한 ----------

    @Test
    void 남의_양에는_올릴_수_없다() throws Exception {
        expectError(putBubble(owner, friendSheep, "안녕", false), 403, "NOT_MY_SHEEP");
    }

    @Test
    void 없는_양은_404() throws Exception {
        expectError(putBubble(owner, 999999L, "안녕", false), 404, "SHEEP_NOT_FOUND");
    }

    @Test
    void 토큰이_없으면_401() throws Exception {
        mvc.perform(put("/api/sheep/{id}/bubble", ownerSheep).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"text\":\"안녕\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("INVALID_DEVICE_TOKEN"));
    }

    // ---------- 24시간 ----------

    @Test
    void 스물네_시간이_지나면_들판_조회에서_빠진다() throws Exception {
        putBubble(owner, ownerSheep, "곧 사라질 말", false).andExpect(status().isOk());

        clock.advance(Duration.ofHours(24).minusSeconds(1));
        mvc.perform(get("/api/pastures/{id}", pastureId).header("X-Device-Token", friend))
                .andExpect(jsonPath("$.sheep[0].bubble.text").value("곧 사라질 말"));

        clock.advance(Duration.ofSeconds(1));
        mvc.perform(get("/api/pastures/{id}", pastureId).header("X-Device-Token", friend))
                .andExpect(jsonPath("$.sheep[0].bubble").value((Object) null));
    }

    @Test
    void 만료된_말풍선_자리에도_새로_올릴_수_있다() throws Exception {
        putBubble(owner, ownerSheep, "옛날 말", false).andExpect(status().isOk());
        clock.advance(Duration.ofHours(25));
        putBubble(owner, ownerSheep, "새 말", false).andExpect(status().isOk());

        mvc.perform(get("/api/pastures/{id}", pastureId).header("X-Device-Token", friend))
                .andExpect(jsonPath("$.sheep[0].bubble.text").value("새 말"));
    }

    // ---------- 지우기 ----------

    @Test
    void 지우면_204이고_들판_조회에서_사라진다() throws Exception {
        putBubble(owner, ownerSheep, "지울 말", false).andExpect(status().isOk());

        mvc.perform(delete("/api/sheep/{id}/bubble", ownerSheep).header("X-Device-Token", owner))
                .andExpect(status().isNoContent());

        mvc.perform(get("/api/pastures/{id}", pastureId).header("X-Device-Token", friend))
                .andExpect(jsonPath("$.sheep[0].bubble").value((Object) null));
    }

    @Test
    void 지울_말풍선이_없으면_404() throws Exception {
        expectError(mvc.perform(delete("/api/sheep/{id}/bubble", ownerSheep).header("X-Device-Token", owner)),
                404, "BUBBLE_NOT_FOUND");
    }

    @Test
    void 이미_지웠거나_만료된_말풍선을_지우면_404() throws Exception {
        putBubble(owner, ownerSheep, "지울 말", false).andExpect(status().isOk());
        mvc.perform(delete("/api/sheep/{id}/bubble", ownerSheep).header("X-Device-Token", owner))
                .andExpect(status().isNoContent());
        expectError(mvc.perform(delete("/api/sheep/{id}/bubble", ownerSheep).header("X-Device-Token", owner)),
                404, "BUBBLE_NOT_FOUND");

        putBubble(owner, ownerSheep, "또 올림", false).andExpect(status().isOk());
        clock.advance(Duration.ofHours(24));
        expectError(mvc.perform(delete("/api/sheep/{id}/bubble", ownerSheep).header("X-Device-Token", owner)),
                404, "BUBBLE_NOT_FOUND");
    }

    @Test
    void 남의_양의_말풍선은_지울_수_없다() throws Exception {
        putBubble(friend, friendSheep, "내 말", false).andExpect(status().isOk());
        expectError(mvc.perform(delete("/api/sheep/{id}/bubble", friendSheep).header("X-Device-Token", owner)),
                403, "NOT_MY_SHEEP");
    }

    // ---------- 단위: 키워드 검사 ----------

    @Test
    void 키워드_검사는_일상적인_글을_잡지_않는다() {
        RiskKeywordChecker checker = new RiskKeywordChecker();
        assertThat(checker.containsRisk("오늘 입사 100일")).isFalse();
        assertThat(checker.containsRisk("점심 뭐 먹지")).isFalse();
        assertThat(checker.containsRisk("자살 예방 캠페인")).isTrue(); // 포함 여부만 본다
    }

    // ---------- helpers ----------

    private ResultActions putBubble(String token, long sheepId, String text, boolean confirm) throws Exception {
        // 테스트 글에는 따옴표·역슬래시가 없어서 이스케이프가 필요 없다
        String body = "{\"text\":\"" + text + "\",\"confirm\":" + confirm + "}";
        return mvc.perform(put("/api/sheep/{id}/bubble", sheepId).header("X-Device-Token", token)
                .contentType(MediaType.APPLICATION_JSON).content(body));
    }

    private String issueToken() throws Exception {
        return com.ganjithon.handeulpan.support.TestUsers.signupNew(mvc);
    }

    private void expectError(ResultActions actions, int status, String code) throws Exception {
        actions.andExpect(status().is(status)).andExpect(jsonPath("$.code").value(code))
                .andExpect(jsonPath("$.message").isNotEmpty());
    }
}
