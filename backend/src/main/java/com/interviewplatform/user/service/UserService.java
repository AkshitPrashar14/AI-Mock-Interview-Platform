package com.interviewplatform.user.service;

import com.interviewplatform.user.dto.request.UpdateProfileRequest;
import com.interviewplatform.user.dto.response.UserResponse;
import com.interviewplatform.user.entity.User;

import java.util.UUID;

/**
 * Contract for user data management operations.
 *
 * <p>The {@code user} module owns all user data — auth delegates to this
 * service for user creation and retrieval.</p>
 *
 * <p><b>Sprint 2 — Authentication & User Management</b></p>
 */
public interface UserService {

    /**
     * Finds a user by their UUID.
     *
     * @param id the user UUID
     * @return user response DTO
     * @throws com.interviewplatform.user.exception.UserNotFoundException if not found
     */
    UserResponse findById(UUID id);

    /**
     * Finds a user entity by their UUID (for internal use by auth module).
     *
     * @param id the user UUID
     * @return the {@link User} entity
     */
    User findEntityById(UUID id);

    /**
     * Returns the currently authenticated user's profile.
     *
     * @param userId the authenticated user's UUID
     * @return user response DTO
     */
    UserResponse getCurrentUser(UUID userId);

    /**
     * Updates the profile fields for the authenticated user.
     *
     * <p>Only firstName, lastName, and profilePictureUrl can be updated.
     * Email, role, and password changes are handled by dedicated endpoints.</p>
     *
     * @param userId  the authenticated user's UUID
     * @param request the update payload
     * @return updated user response DTO
     */
    UserResponse updateProfile(UUID userId, UpdateProfileRequest request);
}
