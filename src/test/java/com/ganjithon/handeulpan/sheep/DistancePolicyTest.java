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
}
