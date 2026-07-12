package com.interviewplatform.user.dto.request;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Request body for {@code PATCH /api/v1/users/profile}.
 *
 * <p>All fields are optional — only non-null values are applied.</p>
 *
 * <p><b>Security constraint:</b> Email, role, and password are NOT present
 * in this DTO. Changing them requires dedicated endpoints.</p>
 *
 * <p><b>Sprint 2 — Authentication & User Management</b></p>
 */
@Getter
@Setter
@NoArgsConstructor
public class UpdateProfileRequest {

    @Size(min = 1, max = 100, message = "First name must be between 1 and 100 characters")
    private String firstName;

    @Size(min = 1, max = 100, message = "Last name must be between 1 and 100 characters")
    private String lastName;

    @Pattern(
            regexp = "^(https?://.*)?$",
            message = "Profile picture URL must be a valid HTTP or HTTPS URL"
    )
    @Size(max = 2048, message = "Profile picture URL must not exceed 2048 characters")
    private String profilePictureUrl;
}
