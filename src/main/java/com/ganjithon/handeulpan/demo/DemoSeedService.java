package com.ganjithon.handeulpan.demo;

import com.ganjithon.handeulpan.auth.AuthService;
import com.ganjithon.handeulpan.auth.dto.SignupRequest;
import com.ganjithon.handeulpan.bubble.Bubble;
import com.ganjithon.handeulpan.bubble.BubbleRepository;
import com.ganjithon.handeulpan.pasture.PastureService;
import com.ganjithon.handeulpan.pasture.dto.CreatePastureRequest;
import com.ganjithon.handeulpan.pasture.dto.JoinPastureRequest;
import com.ganjithon.handeulpan.scarf.ScarfService;
import com.ganjithon.handeulpan.sheep.Distance;
import com.ganjithon.handeulpan.sheep.DistancePolicy;
import com.ganjithon.handeulpan.sheep.Sheep;
import com.ganjithon.handeulpan.sheep.SheepRepository;
import com.ganjithon.handeulpan.sheep.WoolColor;
import com.ganjithon.handeulpan.user.User;
import com.ganjithon.handeulpan.user.UserRepository;
import jakarta.persistence.EntityManager;
import java.time.Clock;
import java.time.Duration;
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
 * <pre>
 * 두부(나)  CREAM    가운데  — 시연에서 로그인하는 계정. 친구들의 목도리를 받아 둔 상태
 * 콩이      MINT     가운데  — 말풍선 있음. 되감기(POST /api/demo/sheep/{id}/rewind)로 멀어지는 모습을 보여 줄 양
 * 보리      YELLOW   산책    — 3일 전에 쓰다듬음
 * 별이      LAVENDER 낮잠    — 3일간 낮잠 중
 * 구름      SKY      가운데  — 말풍선 있음
 * (빈자리 1칸)                — 링크로 합류하는 장면용
 * </pre>
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
    private final ScarfService scarfService;
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
        Member cloud = signup("cloud", "구름");

        String pastureId = pastureService.create(dubu.user,
                new CreatePastureRequest(PASTURE_NAME, "두부", WoolColor.CREAM, true)).pastureId();
        dubu.sheepId = sheepIdOf(pastureId, dubu);
        join(pastureId, kong, WoolColor.MINT);
        join(pastureId, bori, WoolColor.YELLOW);
        join(pastureId, byeol, WoolColor.LAVENDER);
        join(pastureId, cloud, WoolColor.SKY);

        bubble(kong, "시험 끝났다!", now);
        bubble(cloud, "오늘도 맑음", now);

        sheep(bori).rewind(Duration.ofDays(3));          // 산책 중
        sheep(byeol).startNap(now.plus(Duration.ofDays(3))); // 낮잠 중

        // 두부는 친구들에게 목도리를 받아 둔 상태. 마지막에 받은 콩이(MINT) 목도리를 두르고 있습니다.
        scarfService.send(bori.user, dubu.sheepId);
        scarfService.send(byeol.user, dubu.sheepId);
        scarfService.send(kong.user, dubu.sheepId);

        List<SeedMember> members = new ArrayList<>();
        for (Member m : List.of(dubu, kong, bori, byeol, cloud)) {
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

    private void bubble(Member member, String text, Instant now) {
        bubbleRepository.save(new Bubble(sheep(member), text, false, now));
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
