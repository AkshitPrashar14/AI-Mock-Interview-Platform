package com.interviewplatform.user.controller;

import com.interviewplatform.common.constants.ApiConstants;
import com.interviewplatform.common.response.ApiResponse;
import com.interviewplatform.user.dto.request.UpdateProfileRequest;
import com.interviewplatform.user.dto.response.UserResponse;
import com.interviewplatform.user.entity.User;
import com.interviewplatform.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.MDC;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * User profile management endpoints.
 *
 * <p><b>Endpoints:</b></p>
 * <ul>
 *   <li>PATCH /api/v1/users/profile</li>
 * </ul>
 *
 * <p>All endpoints require authentication. Email, role, and password
 * changes are NOT supported through this controller.</p>
 *
 * <p><b>Sprint 2 — Authentication & User Management</b></p>
 */
@RestController
@RequestMapping(ApiConstants.BASE_PATH + "/users")
@RequiredArgsConstructor
@Tag(name = "Users", description = "User profile management")
public class UserController {

    private final UserService userService;

    // =========================================================================
    // PATCH /api/v1/users/profile
    // =========================================================================

    @PatchMapping("/profile")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Update the authenticated user's profile",
               description = "Only firstName, lastName, and profilePictureUrl can be updated. " +
                             "Email, role, and password changes require dedicated endpoints.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Profile updated"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation error"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Not authenticated")
    })
    public ResponseEntity<ApiResponse<UserResponse>> updateProfile(
            @AuthenticationPrincipal User principal,
            @Valid @RequestBody UpdateProfileRequest request,
            HttpServletRequest httpRequest
    ) {
        UserResponse updated = userService.updateProfile(principal.getId(), request);
        return ResponseEntity.ok(
                ApiResponse.success("Profile updated successfully", updated,
                        httpRequest.getRequestURI(), MDC.get(ApiConstants.MDC_REQUEST_ID_KEY))
        );
    }
}
