package com.omegafrog.My.piano.app;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

@TestConfiguration
public class TestUtilConfig {
    @Bean
    public Cleanup cleanup() {
        return new Cleanup();
    }

    @Bean
    public TestUtil testUtil() {
        return new TestUtil();
    }
}
