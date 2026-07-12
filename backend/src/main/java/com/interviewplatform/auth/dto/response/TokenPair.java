package com.interviewplatform.auth.dto.response;

import lombok.Builder;
import lombok.Getter;

/**
 * Represents a refreshed token pair returned by
 * {@code POST /api/v1/auth/refresh}.
 *
 * <p>Only the new access token is returned in the response body.
 * The rotated refresh token is delivered via a new {@code HttpOnly} cookie.</p>
 *
 * <p><b>Sprint 2 — Authentication & User Management</b></p>
 */
@Getter
@Builder
public class TokenPair {

    /** New JWT access token. */
    private final String accessToken;

    /** Access token lifetime in seconds. */
    private final long expiresIn;

    /** Always "Bearer". */
    @Builder.Default
    private final String tokenType = "Bearer";
}
