package com.interviewplatform.security.jwt;

import com.interviewplatform.user.entity.User;

import java.util.UUID;

/**
 * Contract for JWT token operations.
 *
 * <p>Implementations are interchangeable — the current implementation uses HS256.
 * An RS256 implementation can be introduced later by creating a new implementation
 * class and swapping the Spring bean, with zero impact on callers.</p>
 *
 * <p><b>Claim Name Constants</b> — use these instead of inline strings:</p>
 * <ul>
 *   <li>{@link #CLAIM_ROLE}</li>
 *   <li>{@link #CLAIM_EMAIL}</li>
 *   <li>{@link #CLAIM_VERSION}</li>
 * </ul>
 *
 * <p><b>Sprint 2 — Authentication & User Management</b></p>
 */
public interface JwtService {

    // =========================================================================
    // Claim Name Constants — never use raw strings for claim keys
    // =========================================================================

    /** JWT standard subject claim — stores the user UUID. */
    String CLAIM_SUB = "sub";

    /** JWT standard issuer claim. */
    String CLAIM_ISS = "iss";

    /** JWT standard audience claim. */
    String CLAIM_AUD = "aud";

    /** JWT standard JWT ID claim — unique token identifier. */
    String CLAIM_JTI = "jti";

    /** Custom claim — stores the user's role (USER, ADMIN, RECRUITER). */
    String CLAIM_ROLE = "role";

    /** Custom claim — stores the user's email address. */
    String CLAIM_EMAIL = "email";

    /**
     * Custom claim — token schema version.
     * Increment {@code app.jwt.token-version} to invalidate all existing tokens
     * when the payload structure changes.
     */
    String CLAIM_VERSION = "version";

    // =========================================================================
    // Token Generation
    // =========================================================================

    /**
     * Generates a signed access token for the given user.
     *
     * <p>Claims included: sub, email, role, iss, aud, jti, version, iat, exp.</p>
     *
     * @param user the authenticated user
     * @return signed JWT string
     */
    String generateAccessToken(User user);

    // =========================================================================
    // Token Validation
    // =========================================================================

    /**
     * Validates a JWT token string.
     *
     * @param token the JWT string (without "Bearer " prefix)
     * @return {@code true} if the token is structurally valid, unexpired, and
     *         has a valid signature; {@code false} otherwise
     */
    boolean isTokenValid(String token);

    // =========================================================================
    // Claim Extraction
    // =========================================================================

    /**
     * Extracts the user ID ({@code sub} claim) from a validated token.
     *
     * @param token valid JWT string
     * @return user UUID
     */
    UUID extractUserId(String token);

    /**
     * Extracts the email address from a validated token.
     *
     * @param token valid JWT string
     * @return user email
     */
    String extractEmail(String token);

    /**
     * Extracts the role string from a validated token.
     *
     * @param token valid JWT string
     * @return role name (e.g. "USER")
     */
    String extractRole(String token);
}
