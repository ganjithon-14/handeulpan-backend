package com.ganjithon.handeulpan.sheep;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * 양의 거리는 저장하지 않고, 요청이 올 때마다 마지막 쓰다듬은 시각으로 계산합니다. (기획서 8장)
 */
@Component
@RequiredArgsConstructor
public class DistancePolicy {

    private final DistanceProperties properties;
    private final Clock clock;

    public Distance distanceOf(Sheep sheep) {
        Instant now = clock.instant();
        if (sheep.isNapping(now)) {
            return Distance.CENTER; // 낮잠 중에는 멀어지지 않습니다
        }
        return distanceOf(sheep.distanceClockStart(), now);
    }

    public boolean isNapping(Sheep sheep) {
        return sheep.isNapping(clock.instant());
    }

    Distance distanceOf(Instant lastPattedAt, Instant now) {
        Duration elapsed = Duration.between(lastPattedAt, now);
        if (elapsed.compareTo(properties.edgeAfter()) >= 0) {
            return Distance.EDGE;
        }
        if (elapsed.compareTo(properties.walkAfter()) >= 0) {
            return Distance.WALK;
        }
        return Distance.CENTER;
    }

    /** 오늘(Asia/Seoul 날짜) 쓰다듬었는지. */
    public boolean pattedToday(Sheep sheep) {
        LocalDate today = LocalDate.now(clock);
        return LocalDate.ofInstant(sheep.getLastPattedAt(), clock.getZone()).equals(today);
    }
}
