package com.ganjithon.handeulpan.sheep;

import com.ganjithon.handeulpan.pasture.CoreError;
import com.ganjithon.handeulpan.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * 양 조회 + 권한 확인을 한 곳에 모은 것. BE-B(말풍선·목도리)도 이걸 그대로 쓰면 됩니다.
 *
 * <pre>{@code
 * Sheep mine   = sheepFinder.requireMine(me, sheepId);        // 내 양만 (404 / 403 NOT_MY_SHEEP)
 * Sheep target = sheepFinder.requireVisibleTo(me, sheepId);   // 같은 들판 멤버의 양 (404 / 403 NOT_PASTURE_MEMBER)
 * Sheep from   = sheepFinder.requireMineInSamePasture(me, target); // 그 들판에서의 내 양
 * }</pre>
 */
@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SheepFinder {

    private final SheepRepository sheepRepository;

    /** 들판을 나간 양은 없는 양으로 취급합니다. */
    public Sheep require(Long sheepId) {
        return sheepRepository.findById(sheepId)
                .filter(Sheep::isActive)
                .orElseThrow(CoreError.SHEEP_NOT_FOUND::exception);
    }

    public Sheep requireMine(User me, Long sheepId) {
        Sheep sheep = require(sheepId);
        if (!sheep.isOwnedBy(me)) {
            throw CoreError.NOT_MY_SHEEP.exception();
        }
        return sheep;
    }

    public Sheep requireVisibleTo(User me, Long sheepId) {
        Sheep sheep = require(sheepId);
        requireMineInSamePasture(me, sheep);
        return sheep;
    }

    public Sheep requireMineInSamePasture(User me, Sheep other) {
        return sheepRepository.findByPastureIdAndUserIdAndLeftAtIsNull(other.getPasture().getId(), me.getId())
                .orElseThrow(CoreError.NOT_PASTURE_MEMBER::exception);
    }
}
