package com.interviewplatform.auth.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Request body for {@code POST /api/v1/auth/login}.
 *
 * <p>Password strength is NOT validated here — login should accept any
 * password and return a generic error for invalid credentials.</p>
 *
 * <p><b>Sprint 2 — Authentication & User Management</b></p>
 */
@Getter
@Setter
@NoArgsConstructor
public class LoginRequest {

    @NotBlank(message = "Email is required")
    @Email(message = "Email must be a valid email address")
    private String email;

    @NotBlank(message = "Password is required")
    private String password;
}
