package com.ganjithon.handeulpan.demo;

import com.ganjithon.handeulpan.auth.AuthService;
import com.ganjithon.handeulpan.auth.dto.SignupRequest;
import com.ganjithon.handeulpan.bubble.Bubble;
import com.ganjithon.handeulpan.bubble.BubbleRepository;
import com.ganjithon.handeulpan.pasture.PastureService;
import com.ganjithon.handeulpan.pasture.dto.CreatePastureRequest;
import com.ganjithon.handeulpan.pasture.dto.JoinPastureRequest;
import com.ganjithon.handeulpan.sheep.Distance;
import com.ganjithon.handeulpan.sheep.DistancePolicy;
import com.ganjithon.handeulpan.sheep.Sheep;
import com.ganjithon.handeulpan.sheep.SheepRepository;
import com.ganjithon.handeulpan.sheep.WoolColor;
import com.ganjithon.handeulpan.user.User;
import com.ganjithon.handeulpan.user.UserRepository;
import jakarta.persistence.EntityManager;
import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 시연용 시드 데이터. 호출할 때마다 이전 데모 데이터를 지우고 같은 상태로 새로 만듭니다.
 *
 * <p>일반 가입·들판·목도리 서비스를 그대로 거쳐서 만들기 때문에 실제 사용자와 같은 규칙(6마리 제한,
 * 목도리 두르기 등)이 적용됩니다. 데모 계정은 이메일이 {@value #EMAIL_DOMAIN}으로 끝나는 것만이고,
 * 정리할 때도 이 계정과 그 들판만 지웁니다.
 *
 * <p>기획서 7장 시연 시나리오(A→B→C)의 시작 상태입니다. 폰 3대에 친구 양을 띄우고, 두부는 다른 기기에서 씁니다.
 * <pre>
 * 두부   CREAM  가운데 — 말풍선 "요즘 좀 지침"(위험 표시). 시연에서 4일 되감기로 가장자리로 보낼 양
 * 콩이   MINT   가운데 — 말풍선 "오늘 입사 100일"
 * 보리   PINK   가운데
 * 별이   YELLOW 가운데
 * (빈자리 2칸)          — 링크로 합류하는 장면용
 * </pre>
 *
 * <p>목도리는 일부러 하나도 보내 두지 않습니다. B에서 친구 셋이 직접 보내고, C에서 두부가 받은 것 중
 * 민트색을 골라 두르는 장면이 시연의 핵심이기 때문입니다. 말풍선은 24시간 뒤 사라지므로 시연 직전에 만드세요.
 */
@Service
@RequiredArgsConstructor
public class DemoSeedService {

    static final String EMAIL_DOMAIN = "@demo.test";
    static final String PASSWORD = "demo1234!";
    static final String PASTURE_NAME = "한 들판 데모";

    private final DemoService demoService;
    private final AuthService authService;
    private final PastureService pastureService;
    private final UserRepository userRepository;
    private final SheepRepository sheepRepository;
    private final BubbleRepository bubbleRepository;
    private final DistancePolicy distancePolicy;
    private final EntityManager em;
    private final Clock clock;

    /** 데모 데이터를 지우고 시연 상태로 다시 만듭니다. */
    @Transactional
    public SeedResponse seed(String demoKey) {
        demoService.checkKey(demoKey);
        removeAll();
        Instant now = clock.instant();

        Member dubu = signup("doobu", "두부");
        Member kong = signup("kong", "콩이");
        Member bori = signup("bori", "보리");
        Member byeol = signup("byeol", "별이");

        String pastureId = pastureService.create(dubu.user,
                new CreatePastureRequest(PASTURE_NAME, "두부", WoolColor.CREAM, true)).pastureId();
        dubu.sheepId = sheepIdOf(pastureId, dubu);
        join(pastureId, kong, WoolColor.MINT);
        join(pastureId, bori, WoolColor.PINK);
        join(pastureId, byeol, WoolColor.YELLOW);

        bubble(dubu, "요즘 좀 지침", true, now);
        bubble(kong, "오늘 입사 100일", false, now);

        List<SeedMember> members = new ArrayList<>();
        for (Member m : List.of(dubu, kong, bori, byeol)) {
            Sheep s = sheep(m);
            members.add(new SeedMember(m.email(), s.getName(), s.getId(), distancePolicy.distanceOf(s),
                    distancePolicy.isNapping(s), m == dubu));
        }
        return new SeedResponse(pastureId, PASSWORD, members);
    }

    /** 데모 계정과 그 들판의 모든 기록을 지웁니다. 데모 데이터가 없으면 아무것도 하지 않습니다. */
    @Transactional
    public void clear(String demoKey) {
        demoService.checkKey(demoKey);
        removeAll();
    }

    private void removeAll() {
        List<Long> userIds = em.createQuery(
                        "select a.user.id from Account a where a.email like :suffix", Long.class)
                .setParameter("suffix", "%" + EMAIL_DOMAIN).getResultList();
        if (userIds.isEmpty()) {
            return;
        }
        List<Long> pastureIds = em.createQuery(
                        "select p.id from Pasture p where p.owner.id in :users", Long.class)
                .setParameter("users", userIds).getResultList();
        // 데모 들판에 들어와 있던 다른 사람의 양도 함께 지웁니다. (들판이 없어지므로)
        List<Long> sheepIds = pastureIds.isEmpty()
                ? em.createQuery("select s.id from Sheep s where s.user.id in :users", Long.class)
                        .setParameter("users", userIds).getResultList()
                : em.createQuery("select s.id from Sheep s where s.user.id in :users or s.pasture.id in :pastures",
                                Long.class)
                        .setParameter("users", userIds).setParameter("pastures", pastureIds).getResultList();

        if (!sheepIds.isEmpty()) {
            // 목도리를 지우기 전에, 그 목도리를 두르고 있는 기록부터 지웁니다.
            em.createQuery("delete from WornScarf w where w.sheepId in :sheep or w.scarf.id in "
                            + "(select c.id from Scarf c where c.fromSheep.id in :sheep or c.toSheep.id in :sheep)")
                    .setParameter("sheep", sheepIds).executeUpdate();
            em.createQuery("delete from Scarf c where c.fromSheep.id in :sheep or c.toSheep.id in :sheep")
                    .setParameter("sheep", sheepIds).executeUpdate();
            em.createQuery("delete from Bubble b where b.sheep.id in :sheep")
                    .setParameter("sheep", sheepIds).executeUpdate();
            em.createQuery("delete from Sheep s where s.id in :sheep")
                    .setParameter("sheep", sheepIds).executeUpdate();
        }
        if (!pastureIds.isEmpty()) {
            em.createQuery("delete from Pasture p where p.id in :pastures")
                    .setParameter("pastures", pastureIds).executeUpdate();
        }
        em.createQuery("delete from Account a where a.user.id in :users")
                .setParameter("users", userIds).executeUpdate();
        em.createQuery("delete from User u where u.id in :users")
                .setParameter("users", userIds).executeUpdate();
        em.flush();
        em.clear();
    }

    private Member signup(String id, String sheepName) {
        String email = id + EMAIL_DOMAIN;
        String token = authService.signup(new SignupRequest(email, PASSWORD));
        User user = userRepository.findByDeviceToken(token).orElseThrow();
        return new Member(email, sheepName, user);
    }

    private void join(String pastureId, Member member, WoolColor color) {
        member.sheepId = pastureService
                .join(member.user, pastureId, new JoinPastureRequest(member.sheepName, color, true)).sheepId();
    }

    private Long sheepIdOf(String pastureId, Member member) {
        return sheepRepository.findAllOfUser(member.user.getId()).stream()
                .filter(s -> s.getPasture().getPublicId().equals(pastureId))
                .findFirst().orElseThrow().getId();
    }

    private Sheep sheep(Member member) {
        return sheepRepository.findById(member.sheepId).orElseThrow();
    }

    private void bubble(Member member, String text, boolean flagged, Instant now) {
        bubbleRepository.save(new Bubble(sheep(member), text, flagged, now));
    }

    private static final class Member {
        private final String email;
        private final String sheepName;
        private final User user;
        private Long sheepId;

        private Member(String email, String sheepName, User user) {
            this.email = email;
            this.sheepName = sheepName;
            this.user = user;
        }

        private String email() {
            return email;
        }
    }

    /** pastureId는 링크(/p/{pastureId})에 쓰는 공개 ID, password는 모든 데모 계정이 같은 값입니다. */
    public record SeedResponse(String pastureId, String password, List<SeedMember> members) {
    }

    /** me가 true인 계정으로 로그인하면 시연 시작 화면(두부)이 됩니다. */
    public record SeedMember(String email, String sheepName, Long sheepId, Distance distance, boolean napping,
                             boolean me) {
    }
}
