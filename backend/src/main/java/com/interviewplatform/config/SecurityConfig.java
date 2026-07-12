package com.interviewplatform.config;

import com.interviewplatform.security.filter.JwtAuthenticationFilter;
import com.interviewplatform.security.handler.JwtAccessDeniedHandler;
import com.interviewplatform.security.handler.JwtAuthenticationEntryPoint;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

/**
 * Production-grade Spring Security configuration.
 *
 * <p>Replaces the Sprint 1 permit-all configuration.</p>
 *
 * <p><b>Public endpoints (no authentication required):</b></p>
 * <ul>
 *   <li>{@code /api/v1/auth/**}    — registration, login, refresh, logout</li>
 *   <li>{@code /api/v1/health}     — health check</li>
 *   <li>{@code /swagger-ui/**}     — Swagger UI</li>
 *   <li>{@code /v3/api-docs/**}    — OpenAPI spec</li>
 * </ul>
 *
 * <p>All other endpoints require a valid JWT Bearer token.</p>
 *
 * <p><b>Sprint 2 — Authentication & User Management</b></p>
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final JwtAuthenticationEntryPoint authenticationEntryPoint;
    private final JwtAccessDeniedHandler accessDeniedHandler;

    @Value("${app.cors.allowed-origins:http://localhost:3000}")
    private String allowedOrigins;

    @Value("${app.cors.allowed-methods:GET,POST,PUT,PATCH,DELETE,OPTIONS}")
    private String allowedMethods;

    @Value("${app.cors.allowed-headers:Authorization,Content-Type,X-Request-ID}")
    private String allowedHeaders;

    @Value("${app.cors.allow-credentials:true}")
    private boolean allowCredentials;

    @Value("${app.cors.max-age:3600}")
    private long maxAge;

    // =========================================================================
    // Public endpoint patterns
    // =========================================================================

    private static final String[] PUBLIC_PATHS = {
            "/api/v1/auth/**",
            "/api/v1/health",
            "/swagger-ui/**",
            "/swagger-ui.html",
            "/v3/api-docs/**",
            "/api-docs/**",
            "/actuator/health"
    };

    // =========================================================================
    // Security filter chain
    // =========================================================================

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // Disable CSRF — stateless REST API; no session cookies for auth
            .csrf(AbstractHttpConfigurer::disable)

            // Disable HTTP Basic — no browser auth popups
            .httpBasic(AbstractHttpConfigurer::disable)

            // Enable CORS with our custom configuration
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))

            // Stateless session — JWT carries all authentication state
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

            // Authorization rules
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(PUBLIC_PATHS).permitAll()
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                .anyRequest().authenticated()
            )

            // Custom JSON error responses
            .exceptionHandling(ex -> ex
                .authenticationEntryPoint(authenticationEntryPoint)
                .accessDeniedHandler(accessDeniedHandler)
            )

            // JWT filter before username/password authentication
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    // =========================================================================
    // CORS configuration
    // =========================================================================

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOriginPatterns(Arrays.asList(allowedOrigins.split(",")));
        config.setAllowedMethods(Arrays.asList(allowedMethods.split(",")));
        config.setAllowedHeaders(Arrays.asList(allowedHeaders.split(",")));
        config.setAllowCredentials(allowCredentials);
        config.setMaxAge(maxAge);
        config.setExposedHeaders(List.of("X-Request-ID"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
