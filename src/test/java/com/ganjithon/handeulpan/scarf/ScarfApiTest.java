package com.ganjithon.handeulpan.scarf;

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

/**
 * F7 목도리: 보내기(색·하루 1회·자기 자신·멤버), 받으면 자동으로 두르기, 두르기·벗기, 들판 조회 반영.
 * 시작 시각은 2026-11-10 09:00 KST.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(TestClockConfig.class)
@Transactional
class ScarfApiTest {

    @Autowired
    MockMvc mvc;

    @Autowired
    MutableClock clock;

    String owner;   // 두부, CREAM
    String friend;  // 콩이, MINT
    String pastureId;
    long ownerSheep;
    long friendSheep;

    @BeforeEach
    void setUp() throws Exception {
        clock.set(TestClockConfig.START);
        owner = signup("두부");
        friend = signup("콩이");
        MvcResult created = mvc.perform(post("/api/pastures").header("X-Device-Token", owner)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"동기\",\"woolColor\":\"CREAM\",\"agreed\":true}"))
                .andExpect(status().isCreated()).andReturn();
        pastureId = JsonPath.read(created.getResponse().getContentAsString(), "$.pastureId");
        ownerSheep = idOf(created, "$.sheepId");
        friendSheep = join(friend, "MINT");
    }

    // ---------- 보내기 ----------

    @Test
    void 목도리_색은_보내는_양의_털_색이다() throws Exception {
        send(owner, friendSheep)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.color").value("CREAM"));
        send(friend, ownerSheep)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.color").value("MINT"));
    }

    @Test
    void 자기_자신에게는_보낼_수_없다() throws Exception {
        expectError(send(owner, ownerSheep), 400, "SELF");
    }

    @Test
    void 같은_친구에게_하루에_한_번만_보낼_수_있다() throws Exception {
        send(owner, friendSheep).andExpect(status().isCreated());
        expectError(send(owner, friendSheep), 409, "ALREADY_SENT_TODAY");
    }

    @Test
    void 하루는_서울_날짜_기준이라_자정이_지나면_다시_보낼_수_있다() throws Exception {
        send(owner, friendSheep).andExpect(status().isCreated()); // 11/10 09:00

        clock.advance(Duration.ofHours(14).plusMinutes(59)); // 11/10 23:59
        expectError(send(owner, friendSheep), 409, "ALREADY_SENT_TODAY");

        clock.advance(Duration.ofMinutes(1)); // 11/11 00:00
        send(owner, friendSheep).andExpect(status().isCreated());
    }

    @Test
    void 다른_친구에게는_같은_날_또_보낼_수_있다() throws Exception {
        String third = signup("별이");
        long thirdSheep = join(third, "PINK");

        send(owner, friendSheep).andExpect(status().isCreated());
        send(owner, thirdSheep).andExpect(status().isCreated());
    }

    @Test
    void 들판_멤버가_아니면_보낼_수_없다() throws Exception {
        String stranger = signup("낯선이");
        expectError(send(stranger, friendSheep), 403, "NOT_PASTURE_MEMBER");
    }

    @Test
    void 없는_양에게는_보낼_수_없다() throws Exception {
        expectError(send(owner, 999999L), 404, "SHEEP_NOT_FOUND");
    }

    @Test
    void 토큰이_없으면_401() throws Exception {
        mvc.perform(post("/api/sheep/{id}/scarves", friendSheep))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("INVALID_DEVICE_TOKEN"));
    }

    // ---------- 받으면 자동으로 두름 ----------

    @Test
    void 받으면_가장_최근_목도리를_자동으로_두른다() throws Exception {
        pasture(friend).andExpect(jsonPath("$.sheep[1].wornScarf").value((Object) null));

        send(owner, friendSheep).andExpect(status().isCreated());

        // 받은 사람 자신과 들판 친구 모두에게 두른 색이 보인다
        pasture(friend).andExpect(jsonPath("$.sheep[1].wornScarf").value("CREAM"));
        pasture(owner).andExpect(jsonPath("$.sheep[1].wornScarf").value("CREAM"));
        // 보낸 쪽은 영향이 없다
        pasture(owner).andExpect(jsonPath("$.sheep[0].wornScarf").value((Object) null));
    }

    @Test
    void 새로_받으면_이전에_두른_목도리를_바꿔서_두른다() throws Exception {
        String third = signup("별이");
        long thirdSheep = join(third, "PINK");

        send(owner, thirdSheep).andExpect(status().isCreated());   // CREAM
        send(friend, thirdSheep).andExpect(status().isCreated());  // MINT

        pasture(third).andExpect(jsonPath("$.sheep[2].wornScarf").value("MINT"));
    }

    // ---------- 들판 조회: myScarves, sentScarfToday ----------

    @Test
    void 받은_목도리는_myScarves로_내려가고_최근_것이_먼저다() throws Exception {
        String third = signup("별이");
        join(third, "PINK");
        long thirdSheep = join(signup("하늘"), "SKY");

        send(owner, thirdSheep).andExpect(status().isCreated());   // CREAM
        send(friend, thirdSheep).andExpect(status().isCreated());  // MINT

        String token = tokenOfSheepOwner(thirdSheep);
        pasture(token)
                .andExpect(jsonPath("$.myScarves.length()").value(2))
                .andExpect(jsonPath("$.myScarves[0].color").value("MINT"))
                .andExpect(jsonPath("$.myScarves[1].color").value("CREAM"))
                .andExpect(jsonPath("$.myScarves[0].id").isNumber())
                // 보낸 사람과 시각은 내보내지 않는다
                .andExpect(jsonPath("$.myScarves[0].fromSheep").doesNotExist())
                .andExpect(jsonPath("$.myScarves[0].createdAt").doesNotExist());
    }

    @Test
    void 같은_색은_하나만_내려가서_받은_개수가_드러나지_않는다() throws Exception {
        long mintSender = join(signup("하나"), "MINT"); // 친구(콩이)와 같은 MINT
        String receiver = signup("별이");
        long receiverSheep = join(receiver, "PINK");

        send(friend, receiverSheep).andExpect(status().isCreated());
        clock.advance(Duration.ofSeconds(1));
        sendAs(mintSender, receiverSheep);

        pasture(receiver)
                .andExpect(jsonPath("$.myScarves.length()").value(1))
                .andExpect(jsonPath("$.myScarves[0].color").value("MINT"));
    }

    @Test
    void 받은_게_없으면_myScarves는_빈_배열이다() throws Exception {
        pasture(owner).andExpect(jsonPath("$.myScarves.length()").value(0));
    }

    @Test
    void 오늘_보낸_친구만_sentScarfToday가_true이고_내_양에는_필드가_없다() throws Exception {
        pasture(owner)
                .andExpect(jsonPath("$.sheep[1].sentScarfToday").value(false))
                .andExpect(jsonPath("$.sheep[0].sentScarfToday").doesNotExist());

        send(owner, friendSheep).andExpect(status().isCreated());
        pasture(owner).andExpect(jsonPath("$.sheep[1].sentScarfToday").value(true));
        // 받은 쪽에게는 보낸 쪽이 보냈는지가 드러나지 않는다
        pasture(friend).andExpect(jsonPath("$.sheep[0].sentScarfToday").value(false));

        clock.advance(Duration.ofDays(1));
        pasture(owner).andExpect(jsonPath("$.sheep[1].sentScarfToday").value(false));
    }

    // ---------- 두르기·벗기 ----------

    @Test
    void 받은_목도리_중에서_골라_두를_수_있다() throws Exception {
        long thirdSheep = join(signup("하늘"), "SKY");
        send(owner, thirdSheep).andExpect(status().isCreated());   // CREAM
        send(friend, thirdSheep).andExpect(status().isCreated());  // MINT (자동으로 두름)
        String token = tokenOfSheepOwner(thirdSheep);

        long creamId = ((Number) JsonPath.read(pasture(token).andReturn().getResponse().getContentAsString(),
                "$.myScarves[1].id")).longValue();
        wear(token, thirdSheep, "" + creamId)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.wornScarf").value("CREAM"));

        pasture(owner).andExpect(jsonPath("$.sheep[2].wornScarf").value("CREAM"));
    }

    @Test
    void 벗으면_wornScarf가_null이고_다시_받으면_자동으로_두른다() throws Exception {
        send(owner, friendSheep).andExpect(status().isCreated());

        wear(friend, friendSheep, "null")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.wornScarf").value((Object) null));
        pasture(owner).andExpect(jsonPath("$.sheep[1].wornScarf").value((Object) null));

        clock.advance(Duration.ofDays(1));
        send(owner, friendSheep).andExpect(status().isCreated());
        pasture(owner).andExpect(jsonPath("$.sheep[1].wornScarf").value("CREAM"));
    }

    @Test
    void scarfId를_생략해도_벗는다() throws Exception {
        send(owner, friendSheep).andExpect(status().isCreated());

        mvc.perform(put("/api/sheep/{id}/scarf", friendSheep).header("X-Device-Token", friend)
                        .contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.wornScarf").value((Object) null));
    }

    @Test
    void 벗은_상태에서_또_벗어도_괜찮다() throws Exception {
        wear(friend, friendSheep, "null").andExpect(status().isOk());
        wear(friend, friendSheep, "null").andExpect(status().isOk());
    }

    @Test
    void 내_양이_받은_목도리가_아니면_404() throws Exception {
        send(owner, friendSheep).andExpect(status().isCreated()); // 콩이가 받음
        long friendScarfId = ((Number) JsonPath.read(pasture(friend).andReturn().getResponse().getContentAsString(),
                "$.myScarves[0].id")).longValue();

        // 두부는 그 목도리를 받은 적이 없다
        expectError(wear(owner, ownerSheep, "" + friendScarfId), 404, "SCARF_NOT_FOUND");
        expectError(wear(owner, ownerSheep, "999999"), 404, "SCARF_NOT_FOUND");
    }

    @Test
    void 남의_양의_목도리는_바꿀_수_없다() throws Exception {
        expectError(wear(owner, friendSheep, "null"), 403, "NOT_MY_SHEEP");
    }

    @Test
    void 없는_양의_목도리는_404() throws Exception {
        expectError(wear(owner, 999999L, "null"), 404, "SHEEP_NOT_FOUND");
    }

    // ---------- helpers ----------

    private ResultActions send(String token, long toSheepId) throws Exception {
        return mvc.perform(post("/api/sheep/{id}/scarves", toSheepId).header("X-Device-Token", token));
    }

    /** 이미 가입된 양의 주인 토큰을 몰라도 쓸 수 있게, 양 id로 주인을 찾아 보낸다. */
    private void sendAs(long fromSheepId, long toSheepId) throws Exception {
        send(tokenOfSheepOwner(fromSheepId), toSheepId).andExpect(status().isCreated());
    }

    private ResultActions wear(String token, long sheepId, String scarfIdJson) throws Exception {
        return mvc.perform(put("/api/sheep/{id}/scarf", sheepId).header("X-Device-Token", token)
                .contentType(MediaType.APPLICATION_JSON).content("{\"scarfId\":" + scarfIdJson + "}"));
    }

    private ResultActions pasture(String token) throws Exception {
        return mvc.perform(get("/api/pastures/{id}", pastureId).header("X-Device-Token", token))
                .andExpect(status().isOk());
    }

    private final java.util.Map<Long, String> tokensBySheep = new java.util.HashMap<>();

    private long join(String token, String woolColor) throws Exception {
        MvcResult joined = mvc.perform(post("/api/pastures/{id}/sheep", pastureId).header("X-Device-Token", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"woolColor\":\"" + woolColor
                                + "\",\"agreed\":true}"))
                .andExpect(status().isCreated()).andReturn();
        long id = idOf(joined, "$.sheepId");
        tokensBySheep.put(id, token);
        return id;
    }

    private String tokenOfSheepOwner(long sheepId) {
        if (sheepId == ownerSheep) {
            return owner;
        }
        if (sheepId == friendSheep) {
            return friend;
        }
        return tokensBySheep.get(sheepId);
    }

    private long idOf(MvcResult result, String path) throws Exception {
        return ((Number) JsonPath.read(result.getResponse().getContentAsString(), path)).longValue();
    }

    private String signup(String nickname) throws Exception {
        return com.ganjithon.handeulpan.support.TestUsers.signup(mvc, nickname);
    }

    private void expectError(ResultActions actions, int status, String code) throws Exception {
        actions.andExpect(status().is(status)).andExpect(jsonPath("$.code").value(code))
                .andExpect(jsonPath("$.message").isNotEmpty());
    }
}
