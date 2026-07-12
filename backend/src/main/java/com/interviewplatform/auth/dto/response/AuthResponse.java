package com.interviewplatform.auth.dto.response;

import com.interviewplatform.user.dto.response.UserResponse;
import lombok.Builder;
import lombok.Getter;

/**
 * Response body for {@code POST /api/v1/auth/register} and
 * {@code POST /api/v1/auth/login}.
 *
 * <p><b>Refresh token delivery:</b> The refresh token is NEVER included here.
 * It is delivered exclusively via an {@code HttpOnly; Secure; SameSite=Strict}
 * cookie to prevent JavaScript access.</p>
 *
 * <p><b>Sprint 2 — Authentication & User Management</b></p>
 */
@Getter
@Builder
public class AuthResponse {

    /** Authenticated user's public profile. */
    private final UserResponse user;

    /** JWT access token — short-lived (15 minutes). */
    private final String accessToken;

    /** Access token lifetime in seconds (900 = 15 minutes). */
    private final long expiresIn;

    /** Always "Bearer". Included for RFC 6750 compliance. */
    @Builder.Default
    private final String tokenType = "Bearer";
}
