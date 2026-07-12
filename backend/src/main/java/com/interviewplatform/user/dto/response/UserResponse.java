package com.interviewplatform.user.dto.response;

import com.interviewplatform.user.entity.Role;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

/**
 * Public-facing user representation.
 *
 * <p>This DTO is the only user object returned in API responses.
 * It deliberately omits {@code passwordHash} and all internal security fields.</p>
 *
 * <p><b>Sprint 2 — Authentication & User Management</b></p>
 */
@Getter
@Builder
public class UserResponse {

    private final UUID id;
    private final String firstName;
    private final String lastName;
    private final String email;
    private final Role role;
    private final boolean isActive;
    private final boolean isEmailVerified;
    private final String profilePictureUrl;
    private final Instant createdAt;
    private final Instant updatedAt;
    private final Instant lastLoginAt;
}
