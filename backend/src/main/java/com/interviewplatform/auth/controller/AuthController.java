package com.interviewplatform.auth.controller;

import com.interviewplatform.auth.dto.request.LoginRequest;
import com.interviewplatform.auth.dto.request.RegisterRequest;
import com.interviewplatform.auth.dto.response.AuthResponse;
import com.interviewplatform.auth.dto.response.TokenPair;
import com.interviewplatform.auth.service.AuthService;
import com.interviewplatform.common.constants.ApiConstants;
import com.interviewplatform.common.response.ApiResponse;
import com.interviewplatform.security.jwt.JwtProperties;
import com.interviewplatform.security.jwt.JwtService;
import com.interviewplatform.user.dto.response.UserResponse;
import com.interviewplatform.user.entity.User;
import com.interviewplatform.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * Authentication endpoints.
 *
 * <p><b>Endpoints:</b></p>
 * <ul>
 *   <li>POST /api/v1/auth/register</li>
 *   <li>POST /api/v1/auth/login</li>
 *   <li>POST /api/v1/auth/refresh</li>
 *   <li>POST /api/v1/auth/logout</li>
 *   <li>GET  /api/v1/auth/me</li>
 * </ul>
 *
 * <p>Refresh tokens are delivered and consumed exclusively via
 * {@code HttpOnly; Secure; SameSite=Strict} cookies.</p>
 *
 * <p><b>Sprint 2 — Authentication & User Management</b></p>
 */
@RestController
@RequestMapping(ApiConstants.BASE_PATH + "/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "User registration, login, token refresh, and logout")
public class AuthController {

    private static final String REFRESH_TOKEN_COOKIE = "refreshToken";

    private final AuthService authService;
    private final UserService userService;
    private final JwtService jwtService;
    private final JwtProperties jwtProperties;

    // =========================================================================
    // POST /api/v1/auth/register
    // =========================================================================

    @PostMapping("/register")
    @Operation(summary = "Register a new user account")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "User registered successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation error"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Email already in use")
    })
    public ResponseEntity<ApiResponse<AuthResponse>> register(
            @Valid @RequestBody RegisterRequest request,
            HttpServletRequest httpRequest,
            HttpServletResponse httpResponse
    ) {
        AuthResponse auth = authService.register(request, httpRequest);
        String rawRefreshToken = authService.generateAndStoreRefreshToken(auth.getUser().getId(), httpRequest);
        setRefreshTokenCookie(httpResponse, rawRefreshToken);

        return ResponseEntity.status(HttpStatus.CREATED).body(
                ApiResponse.success("Registration successful", auth,
                        httpRequest.getRequestURI(), MDC.get(ApiConstants.MDC_REQUEST_ID_KEY))
        );
    }

    // =========================================================================
    // POST /api/v1/auth/login
    // =========================================================================

    @PostMapping("/login")
    @Operation(summary = "Authenticate with email and password")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Login successful"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation error"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Invalid credentials")
    })
    public ResponseEntity<ApiResponse<AuthResponse>> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletRequest httpRequest,
            HttpServletResponse httpResponse
    ) {
        AuthResponse auth = authService.login(request, httpRequest);
        String rawRefreshToken = authService.generateAndStoreRefreshToken(auth.getUser().getId(), httpRequest);
        setRefreshTokenCookie(httpResponse, rawRefreshToken);

        return ResponseEntity.ok(
                ApiResponse.success("Login successful", auth,
                        httpRequest.getRequestURI(), MDC.get(ApiConstants.MDC_REQUEST_ID_KEY))
        );
    }

    // =========================================================================
    // POST /api/v1/auth/refresh
    // =========================================================================

    @PostMapping("/refresh")
    @Operation(summary = "Refresh access token using the HttpOnly refresh token cookie")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Token refreshed"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Refresh token expired or not found")
    })
    public ResponseEntity<ApiResponse<TokenPair>> refresh(
            @CookieValue(name = REFRESH_TOKEN_COOKIE, required = false) String rawToken,
            HttpServletRequest httpRequest,
            HttpServletResponse httpResponse
    ) {
        if (rawToken == null || rawToken.isBlank()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                    ApiResponse.error("REFRESH_TOKEN_NOT_FOUND",
                            "No refresh token cookie found",
                            httpRequest.getRequestURI(),
                            MDC.get(ApiConstants.MDC_REQUEST_ID_KEY))
            );
        }

        TokenPair pair = authService.refresh(rawToken, httpRequest);
        // Rotate: generate new refresh token
        // Extract userId from the old token's associated user via the pair's accessToken
        String userId = jwtService.extractUserId(pair.getAccessToken()).toString();
        String newRawRefreshToken = authService.generateAndStoreRefreshToken(
                java.util.UUID.fromString(userId), httpRequest);
        setRefreshTokenCookie(httpResponse, newRawRefreshToken);

        return ResponseEntity.ok(
                ApiResponse.success("Token refreshed", pair,
                        httpRequest.getRequestURI(), MDC.get(ApiConstants.MDC_REQUEST_ID_KEY))
        );
    }

    // =========================================================================
    // POST /api/v1/auth/logout
    // =========================================================================

    @PostMapping("/logout")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Logout from the current device (revokes refresh token)")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "204", description = "Logged out"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Not authenticated")
    })
    public ResponseEntity<Void> logout(
            @CookieValue(name = REFRESH_TOKEN_COOKIE, required = false) String rawToken,
            HttpServletRequest httpRequest,
            HttpServletResponse httpResponse
    ) {
        if (rawToken != null) {
            authService.logout(rawToken);
        }
        clearRefreshTokenCookie(httpResponse);
        return ResponseEntity.noContent().build();
    }

    // =========================================================================
    // GET /api/v1/auth/me
    // =========================================================================

    @GetMapping("/me")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Get the currently authenticated user's profile")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Current user returned"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Not authenticated")
    })
    public ResponseEntity<ApiResponse<UserResponse>> me(
            @AuthenticationPrincipal User principal,
            HttpServletRequest httpRequest
    ) {
        UserResponse user = userService.getCurrentUser(principal.getId());
        return ResponseEntity.ok(
                ApiResponse.success("Current user retrieved", user,
                        httpRequest.getRequestURI(), MDC.get(ApiConstants.MDC_REQUEST_ID_KEY))
        );
    }

    // =========================================================================
    // Cookie helpers
    // =========================================================================

    private void setRefreshTokenCookie(HttpServletResponse response, String rawToken) {
        Cookie cookie = new Cookie(REFRESH_TOKEN_COOKIE, rawToken);
        cookie.setHttpOnly(true);
        cookie.setSecure(true);    // HTTPS only in production
        cookie.setPath("/api/v1/auth/refresh");
        cookie.setMaxAge((int) (jwtProperties.getRefreshTokenExpirationDays() * 24L * 60 * 60));
        cookie.setAttribute("SameSite", "Strict");
        response.addCookie(cookie);
    }

    private void clearRefreshTokenCookie(HttpServletResponse response) {
        Cookie cookie = new Cookie(REFRESH_TOKEN_COOKIE, "");
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setPath("/api/v1/auth/refresh");
        cookie.setMaxAge(0);
        response.addCookie(cookie);
    }
}
