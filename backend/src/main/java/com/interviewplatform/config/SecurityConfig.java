package com.interviewplatform.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Sprint 1 — Temporary permissive security configuration.
 *
 * <p><b>Purpose:</b> Keeps the Spring Security infrastructure in place
 * (dependency, filter chain, beans) while permitting all requests without
 * authentication. This allows Sprint 1 endpoints (health, Swagger) to be
 * accessible immediately.</p>
 *
 * <p><b>Sprint 2 will replace this class</b> with a JWT-based filter chain,
 * role-based authorization rules, and BCrypt password encoding.</p>
 *
 * <p>Key decisions for Sprint 1:</p>
 * <ul>
 *   <li>CSRF disabled — REST API, no browser session cookies.</li>
 *   <li>HTTP Basic disabled — prevents 401 challenges in Swagger UI.</li>
 *   <li>Session stateless — prepares the config shape for Sprint 2 JWT.</li>
 *   <li>All requests permitted — no authentication required.</li>
 * </ul>
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    /**
     * Permit-all security filter chain.
     *
     * <p>TODO Sprint 2: Replace {@code .anyRequest().permitAll()} with
     * role-based rules and add the JWT authentication filter.</p>
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // Disable CSRF — not needed for stateless REST APIs
            .csrf(AbstractHttpConfigurer::disable)

            // Disable HTTP Basic — prevents browser auth popups
            .httpBasic(AbstractHttpConfigurer::disable)

            // Stateless session — no server-side session storage
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

            // Sprint 1: permit all requests — replaced in Sprint 2
            .authorizeHttpRequests(auth ->
                auth.anyRequest().permitAll());

        return http.build();
    }
}
