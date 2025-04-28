package com.cookie.app.config;

import org.junit.jupiter.api.Test;

import java.time.Clock;

import static org.assertj.core.api.Assertions.assertThat;

class AppConfigTest {
    private final AppConfig appConfig = new AppConfig();

    @Test
    void test_clockBeanReturnsSystemDefaultZoneClock() {

        Clock clock = appConfig.clock();

        assertThat(clock).isNotNull();
        assertThat(clock.getZone()).isEqualTo(Clock.systemDefaultZone().getZone());
    }
}
