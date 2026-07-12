package com.interviewplatform.auth.exception;

/**
 * Thrown when login credentials (email/password) are invalid.
 *
 * <p>Mapped to HTTP 401 Unauthorized by {@code GlobalExceptionHandler}.</p>
 *
 * <p><b>Security note:</b> The message is intentionally generic to prevent
 * email enumeration attacks.</p>
 *
 * <p><b>Sprint 2 — Authentication & User Management</b></p>
 */
public class InvalidCredentialsException extends RuntimeException {

    public InvalidCredentialsException() {
        super("Invalid email or password");
    }
}
