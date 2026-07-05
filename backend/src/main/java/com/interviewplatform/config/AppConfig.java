package com.interviewplatform.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

/**
 * Core application configuration beans.
 *
 * <p>Centralising the {@link Clock} bean here means all components inject
 * {@code Clock} instead of calling {@code Instant.now()} directly, which
 * makes time-dependent logic trivially testable by injecting a fixed clock
 * in unit tests.</p>
 *
 * <p><b>Sprint 1 — Backend Foundation</b></p>
 */
@Configuration
public class AppConfig {

    /**
     * Application-wide UTC clock.
     *
     * <p>Usage in any Spring-managed component:</p>
     * <pre>
     *   {@code @Autowired private Clock clock;}
     *   {@code Instant now = clock.instant();}
     * </pre>
     *
     * <p>In unit tests, replace with:</p>
     * <pre>
     *   {@code Clock.fixed(Instant.parse("2026-07-05T12:00:00Z"), ZoneOffset.UTC)}
     * </pre>
     */
    @Bean
    public Clock clock() {
        return Clock.systemUTC();
    }
}
