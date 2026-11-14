package com.ganjithon.handeulpan.sheep;

import com.ganjithon.handeulpan.sheep.dto.NapResponse;
import com.ganjithon.handeulpan.sheep.dto.PatResponse;
import com.ganjithon.handeulpan.user.User;
import java.time.Clock;
import java.time.Duration;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SheepService {

    private final SheepFinder sheepFinder;
    private final DistancePolicy distancePolicy;
    private final Clock clock;

    /** 쓰다듬기 = 체크인. 어느 단계에서든 바로 가운데로 돌아옵니다. */
    @Transactional
    public PatResponse pat(User me, Long sheepId) {
        Sheep sheep = sheepFinder.requireMine(me, sheepId);
        sheep.pat(clock.instant());
        return new PatResponse(sheep.getId(), distancePolicy.distanceOf(sheep), true);
    }

    /** 낮잠 모드: 1~7일 동안 멀어지지 않습니다. 이미 낮잠 중이면 지금부터 days일로 다시 정합니다. */
    @Transactional
    public NapResponse nap(User me, Long sheepId, int days) {
        Sheep sheep = sheepFinder.requireMine(me, sheepId);
        sheep.startNap(clock.instant().plus(Duration.ofDays(days)));
        return napResponse(sheep);
    }

    /** 낮잠 깨우기. 낮잠 중이 아니어도 에러 없이 지나갑니다. */
    @Transactional
    public void wakeUp(User me, Long sheepId) {
        sheepFinder.requireMine(me, sheepId).wakeUp(clock.instant());
    }

    /** 들판 나가기: 언제든 한 번에. 다시 들어오려면 링크로 새로 합류합니다. */
    @Transactional
    public void leave(User me, Long sheepId) {
        sheepFinder.requireMine(me, sheepId).leave(clock.instant());
    }

    private NapResponse napResponse(Sheep sheep) {
        return new NapResponse(sheep.getId(), distancePolicy.isNapping(sheep), distancePolicy.distanceOf(sheep));
    }
}
