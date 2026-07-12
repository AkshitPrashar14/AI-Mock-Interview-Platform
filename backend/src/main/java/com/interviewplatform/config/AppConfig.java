package com.interviewplatform.config;

import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Clock;

/**
 * Core application configuration beans.
 *
 * <p>Centralising the {@link Clock} bean here means all components inject
 * {@code Clock} instead of calling {@code Instant.now()} directly, which
 * makes time-dependent logic trivially testable by injecting a fixed clock
 * in unit tests.</p>
 *
 * <p><b>Sprint 1 — Backend Foundation; extended in Sprint 2.</b></p>
 */
@Configuration
@ConfigurationPropertiesScan("com.interviewplatform.security.jwt")
public class AppConfig {

    /**
     * Application-wide UTC clock.
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

    /**
     * BCrypt password encoder with strength 12.
     *
     * <p>Strength 12 provides a good balance between security and login latency
     * (~250ms on modern hardware). Increase to 14 for higher-security environments.</p>
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }

    /**
     * Authentication provider that uses the platform's {@link UserDetailsService}
     * and BCrypt password encoder for credential verification.
     */
    @Bean
    public AuthenticationProvider authenticationProvider(UserDetailsService userDetailsService) {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    /**
     * Exposes the Spring Security {@link AuthenticationManager} as a bean.
     * Required for programmatic authentication in tests and future OAuth2 flows.
     */
    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}
