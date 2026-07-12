package com.interviewplatform.security;

import com.interviewplatform.security.jwt.JwtProperties;
import com.interviewplatform.security.jwt.JwtServiceImpl;
import com.interviewplatform.user.entity.Role;
import com.interviewplatform.user.entity.User;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Date;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link JwtServiceImpl}.
 *
 * <p>No Spring context required — tests the service in isolation using
 * a fixed test secret key and a constructed {@link JwtProperties} instance.</p>
 *
 * <p><b>Sprint 2 — Authentication & User Management</b></p>
 */
@DisplayName("JwtServiceImpl Unit Tests")
class JwtServiceImplTest {

    // Test secret — 256-bit base64-encoded value
    private static final String TEST_SECRET =
            Base64.getEncoder().encodeToString(
                    "test-secret-key-for-unit-tests-only-256bit".getBytes(StandardCharsets.UTF_8));

    private JwtServiceImpl jwtService;
    private JwtProperties properties;
    private User testUser;

    @BeforeEach
    void setUp() {
        properties = new JwtProperties();
        properties.setSecret(TEST_SECRET);
        properties.setAccessTokenExpirationMs(900_000L);
        properties.setIssuer("test-platform");
        properties.setAudience("test-clients");
        properties.setTokenVersion(1);

        jwtService = new JwtServiceImpl(properties);

        testUser = User.builder()
                .id(UUID.randomUUID())
                .email("test@example.com")
                .firstName("Test")
                .lastName("User")
                .passwordHash("$2a$12$hashed")
                .role(Role.USER)
                .isActive(true)
                .build();
    }

    @Nested
    @DisplayName("generateAccessToken")
    class GenerateToken {

        @Test
        @DisplayName("Generated token is non-null and non-blank")
        void tokenIsNotBlank() {
            String token = jwtService.generateAccessToken(testUser);
            assertThat(token).isNotBlank();
        }

        @Test
        @DisplayName("Extracted userId matches user")
        void extractedUserIdMatches() {
            String token = jwtService.generateAccessToken(testUser);
            assertThat(jwtService.extractUserId(token)).isEqualTo(testUser.getId());
        }

        @Test
        @DisplayName("Extracted email matches user")
        void extractedEmailMatches() {
            String token = jwtService.generateAccessToken(testUser);
            assertThat(jwtService.extractEmail(token)).isEqualTo(testUser.getEmail());
        }

        @Test
        @DisplayName("Extracted role matches user")
        void extractedRoleMatches() {
            String token = jwtService.generateAccessToken(testUser);
            assertThat(jwtService.extractRole(token)).isEqualTo("USER");
        }
    }

    @Nested
    @DisplayName("isTokenValid")
    class ValidateToken {

        @Test
        @DisplayName("Valid token returns true")
        void validToken() {
            String token = jwtService.generateAccessToken(testUser);
            assertThat(jwtService.isTokenValid(token)).isTrue();
        }

        @Test
        @DisplayName("Expired token returns false")
        void expiredToken() {
            // Create token already expired
            byte[] keyBytes = Base64.getDecoder().decode(
                    TEST_SECRET.getBytes(StandardCharsets.UTF_8));
            SecretKey key = Keys.hmacShaKeyFor(keyBytes);

            String expiredToken = Jwts.builder()
                    .subject(testUser.getId().toString())
                    .issuedAt(new Date(System.currentTimeMillis() - 2000))
                    .expiration(new Date(System.currentTimeMillis() - 1000)) // already expired
                    .signWith(key)
                    .compact();

            assertThat(jwtService.isTokenValid(expiredToken)).isFalse();
        }

        @Test
        @DisplayName("Malformed token (random string) returns false")
        void malformedToken() {
            assertThat(jwtService.isTokenValid("not.a.valid.jwt.token")).isFalse();
        }

        @Test
        @DisplayName("Token signed with wrong key returns false")
        void invalidSignature() {
            String wrongSecret = Base64.getEncoder().encodeToString(
                    "wrong-secret-completely-different-key!!".getBytes(StandardCharsets.UTF_8));
            byte[] keyBytes = Base64.getDecoder().decode(
                    wrongSecret.getBytes(StandardCharsets.UTF_8));
            SecretKey wrongKey = Keys.hmacShaKeyFor(keyBytes);

            String tampered = Jwts.builder()
                    .subject(testUser.getId().toString())
                    .issuedAt(new Date())
                    .expiration(new Date(System.currentTimeMillis() + 900_000))
                    .signWith(wrongKey)
                    .compact();

            assertThat(jwtService.isTokenValid(tampered)).isFalse();
        }

        @Test
        @DisplayName("Empty string token returns false")
        void emptyToken() {
            assertThat(jwtService.isTokenValid("")).isFalse();
        }

        @Test
        @DisplayName("Null token returns false")
        void nullToken() {
            assertThat(jwtService.isTokenValid(null)).isFalse();
        }
    }
}
