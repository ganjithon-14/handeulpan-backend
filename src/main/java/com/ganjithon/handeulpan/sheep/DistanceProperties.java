package com.ganjithon.handeulpan.sheep;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

/**
 * 거리 규칙 기준. 기획서 초안 값(2일/4일)이 기본이고, 팀 테스트 후 application.yml에서 바꿀 수 있습니다.
 * <pre>
 * handeulpan.distance.walk-after: 2d
 * handeulpan.distance.edge-after: 4d
 * </pre>
 */
@ConfigurationProperties("handeulpan.distance")
public record DistanceProperties(
        @DefaultValue("2d") Duration walkAfter,
        @DefaultValue("4d") Duration edgeAfter) {
}
