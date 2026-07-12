package com.interviewplatform.auth.exception;

/**
 * Thrown when a refresh token is not found in the database,
 * typically because it was already revoked or never existed.
 *
 * <p>Mapped to HTTP 401 Unauthorized by {@code GlobalExceptionHandler}.</p>
 *
 * <p><b>Sprint 2 — Authentication & User Management</b></p>
 */
public class RefreshTokenNotFoundException extends RuntimeException {

    public RefreshTokenNotFoundException() {
        super("Refresh token not found or has been revoked");
    }
}
