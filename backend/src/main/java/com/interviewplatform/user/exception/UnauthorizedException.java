package com.interviewplatform.user.exception;

/**
 * Thrown when an operation requires authentication but the caller
 * is not authenticated or lacks sufficient permissions.
 *
 * <p>Mapped to HTTP 401 Unauthorized by {@code GlobalExceptionHandler}.</p>
 *
 * <p><b>Sprint 2 — Authentication & User Management</b></p>
 */
public class UnauthorizedException extends RuntimeException {

    public UnauthorizedException(String message) {
        super(message);
    }

    public UnauthorizedException() {
        super("Authentication required");
    }
}
