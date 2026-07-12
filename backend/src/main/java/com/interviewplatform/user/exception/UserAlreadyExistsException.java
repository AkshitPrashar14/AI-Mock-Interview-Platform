package com.interviewplatform.user.exception;

/**
 * Thrown when a registration attempt is made with an email address
 * that already belongs to an existing account.
 *
 * <p>Mapped to HTTP 409 Conflict by {@code GlobalExceptionHandler}.</p>
 *
 * <p><b>Sprint 2 — Authentication & User Management</b></p>
 */
public class UserAlreadyExistsException extends RuntimeException {

    public UserAlreadyExistsException(String email) {
        super("An account with email '" + email + "' already exists");
    }
}
