package com.interviewplatform.security.jwt;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Strongly-typed configuration properties for JWT settings.
 *
 * <p>Bound to the {@code app.jwt} prefix in application YAML files.
 * All JWT configuration is consumed through this class — never via
 * {@code @Value} annotations scattered throughout the codebase.</p>
 *
 * <p><b>application.yml example:</b></p>
 * <pre>
 * app:
 *   jwt:
 *     secret: ${JWT_SECRET}
 *     access-token-expiration-ms: 900000
 *     refresh-token-expiration-days: 7
 *     issuer: ai-mock-interview-platform
 *     audience: ai-mock-interview-clients
 *     token-version: 1
 * </pre>
 *
 * <p><b>Sprint 2 — Authentication & User Management</b></p>
 */
@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "app.jwt")
public class JwtProperties {

    /**
     * Base64-encoded HMAC-SHA256 secret key (minimum 256 bits / 32 bytes).
     * Must be supplied via environment variable in production.
     */
    private String secret;

    /**
     * Access token lifetime in milliseconds.
     * Default: 900000 ms (15 minutes).
     */
    private long accessTokenExpirationMs = 900_000L;

    /**
     * Refresh token lifetime in days.
     * Default: 7 days.
     */
    private int refreshTokenExpirationDays = 7;

    /**
     * JWT {@code iss} (issuer) claim value.
     * Identifies the service that issued the token.
     */
    private String issuer = "ai-mock-interview-platform";

    /**
     * JWT {@code aud} (audience) claim value.
     * Identifies the intended recipients of the token.
     */
    private String audience = "ai-mock-interview-clients";

    /**
     * Internal token version claim.
     * Increment to invalidate all existing tokens when the payload structure changes.
     */
    private int tokenVersion = 1;
}
