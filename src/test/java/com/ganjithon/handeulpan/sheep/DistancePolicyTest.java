package com.ganjithon.handeulpan.sheep;

import static org.assertj.core.api.Assertions.assertThat;

import com.ganjithon.handeulpan.common.config.TimeConfig;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import org.junit.jupiter.api.Test;

class DistancePolicyTest {

    private final Instant last = Instant.parse("2026-11-10T00:00:00Z");
    private final DistancePolicy policy = new DistancePolicy(
            new DistanceProperties(Duration.ofDays(2), Duration.ofDays(4)),
            Clock.fixed(last, TimeConfig.SEOUL));

    @Test
    void 이틀_전까지는_가운데() {
        assertThat(policy.distanceOf(last, last)).isEqualTo(Distance.CENTER);
        assertThat(policy.distanceOf(last, last.plus(Duration.ofDays(2)).minusSeconds(1))).isEqualTo(Distance.CENTER);
    }

    @Test
    void 이틀부터_산책_중() {
        assertThat(policy.distanceOf(last, last.plus(Duration.ofDays(2)))).isEqualTo(Distance.WALK);
        assertThat(policy.distanceOf(last, last.plus(Duration.ofDays(4)).minusSeconds(1))).isEqualTo(Distance.WALK);
    }

    @Test
    void 나흘부터_가장자리() {
        assertThat(policy.distanceOf(last, last.plus(Duration.ofDays(4)))).isEqualTo(Distance.EDGE);
        assertThat(policy.distanceOf(last, last.plus(Duration.ofDays(30)))).isEqualTo(Distance.EDGE);
    }

    // ---------- 낮잠 모드 ----------

    private DistancePolicy policyAt(Instant now) {
        return new DistancePolicy(new DistanceProperties(Duration.ofDays(2), Duration.ofDays(4)),
                Clock.fixed(now, TimeConfig.SEOUL));
    }

    private Sheep sheepPattedAt(Instant lastPatted) {
        return new Sheep(null, null, "두부", WoolColor.CREAM, lastPatted);
    }

    @Test
    void 낮잠_중에는_오래_지나도_가운데() {
        Sheep sheep = sheepPattedAt(last);
        sheep.startNap(last.plus(Duration.ofDays(7)));
        Instant sixDaysLater = last.plus(Duration.ofDays(6));

        assertThat(policyAt(sixDaysLater).distanceOf(sheep)).isEqualTo(Distance.CENTER);
        assertThat(policyAt(sixDaysLater).isNapping(sheep)).isTrue();
    }

    @Test
    void 낮잠이_끝난_시점부터_거리를_다시_센다() {
        Sheep sheep = sheepPattedAt(last);
        sheep.startNap(last.plus(Duration.ofDays(3)));

        assertThat(policyAt(last.plus(Duration.ofDays(3))).distanceOf(sheep)).isEqualTo(Distance.CENTER);
        assertThat(policyAt(last.plus(Duration.ofDays(3))).isNapping(sheep)).isFalse();
        assertThat(policyAt(last.plus(Duration.ofDays(5))).distanceOf(sheep)).isEqualTo(Distance.WALK);
        assertThat(policyAt(last.plus(Duration.ofDays(7))).distanceOf(sheep)).isEqualTo(Distance.EDGE);
    }

    @Test
    void 깨우면_그때부터_다시_센다() {
        Sheep sheep = sheepPattedAt(last);
        sheep.startNap(last.plus(Duration.ofDays(7)));
        sheep.wakeUp(last.plus(Duration.ofDays(1)));

        assertThat(policyAt(last.plus(Duration.ofDays(2))).distanceOf(sheep)).isEqualTo(Distance.CENTER);
        assertThat(policyAt(last.plus(Duration.ofDays(3))).distanceOf(sheep)).isEqualTo(Distance.WALK);
    }

    @Test
    void 낮잠이_아닐_때_깨워도_시계가_초기화되지_않는다() {
        Sheep sheep = sheepPattedAt(last);
        Instant threeDays = last.plus(Duration.ofDays(3));
        sheep.wakeUp(threeDays);

        assertThat(policyAt(threeDays).distanceOf(sheep)).isEqualTo(Distance.WALK);
    }

    @Test
    void 낮잠이_끝난_뒤에_쓰다듬으면_쓰다듬은_시각부터_센다() {
        Sheep sheep = sheepPattedAt(last);
        sheep.startNap(last.plus(Duration.ofDays(3)));
        Instant fiveDays = last.plus(Duration.ofDays(5));
        sheep.pat(fiveDays);

        assertThat(policyAt(fiveDays.plus(Duration.ofDays(1))).distanceOf(sheep)).isEqualTo(Distance.CENTER);
    }
}
