package com.ganjithon.handeulpan.support;

import com.ganjithon.handeulpan.common.config.TimeConfig;
import java.time.Instant;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

@TestConfiguration
public class TestClockConfig {

    /** 2026-11-10 09:00 KST */
    public static final Instant START = Instant.parse("2026-11-10T00:00:00Z");

    @Bean
    @Primary
    public MutableClock testClock() {
        return new MutableClock(START, TimeConfig.SEOUL);
    }
}
