package com.interviewplatform.user.exception;

import java.util.UUID;

/**
 * Thrown when a user is looked up by ID but no matching record exists.
 *
 * <p>Mapped to HTTP 404 Not Found by {@code GlobalExceptionHandler}.</p>
 *
 * <p><b>Sprint 2 — Authentication & User Management</b></p>
 */
public class UserNotFoundException extends RuntimeException {

    public UserNotFoundException(UUID userId) {
        super("User not found with id: " + userId);
    }

    public UserNotFoundException(String email) {
        super("User not found with email: " + email);
    }
}
