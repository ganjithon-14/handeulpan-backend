package com.ganjithon.handeulpan.sheep;

import com.ganjithon.handeulpan.sheep.dto.PatResponse;
import com.ganjithon.handeulpan.user.User;
import java.time.Clock;
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
}
