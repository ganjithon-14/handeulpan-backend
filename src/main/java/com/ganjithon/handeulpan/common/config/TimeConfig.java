package com.ganjithon.handeulpan.common.config;

import java.time.Clock;
import java.time.ZoneId;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 서버 시간 기준. 양의 거리 계산, "하루 1회" 같은 날짜 규칙은 모두 이 Clock(Asia/Seoul)을 씁니다.
 * 테스트에서는 고정 Clock으로 바꿔 끼워 시간을 되감거나 넘길 수 있습니다.
 */
@Configuration
public class TimeConfig {

    public static final ZoneId SEOUL = ZoneId.of("Asia/Seoul");

    @Bean
    public Clock clock() {
        return Clock.system(SEOUL);
    }
}
