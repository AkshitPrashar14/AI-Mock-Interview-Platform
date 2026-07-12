package com.interviewplatform.auth.exception;

/**
 * Thrown when a refresh token has passed its expiry date.
 *
 * <p>Mapped to HTTP 401 Unauthorized by {@code GlobalExceptionHandler}.</p>
 *
 * <p><b>Sprint 2 — Authentication & User Management</b></p>
 */
public class RefreshTokenExpiredException extends RuntimeException {

    public RefreshTokenExpiredException() {
        super("Refresh token has expired. Please log in again");
    }
}
