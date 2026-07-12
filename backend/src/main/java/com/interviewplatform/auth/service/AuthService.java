package com.interviewplatform.auth.service;

import com.interviewplatform.auth.dto.request.LoginRequest;
import com.interviewplatform.auth.dto.request.RegisterRequest;
import com.interviewplatform.auth.dto.response.AuthResponse;
import com.interviewplatform.auth.dto.response.TokenPair;
import jakarta.servlet.http.HttpServletRequest;

/**
 * Contract for authentication operations.
 *
 * <p>The auth module is solely responsible for identity verification,
 * token issuance, and session management. User data retrieval is
 * delegated to the {@code user} module.</p>
 *
 * <p><b>Sprint 2 — Authentication & User Management</b></p>
 */
public interface AuthService {

    /**
     * Registers a new user account, hashes the password, and returns
     * an auth response with access token.
     *
     * @param request  registration payload
     * @param httpRequest the HTTP request (used to extract IP and User-Agent)
     * @return auth response containing user profile and access token
     * @throws com.interviewplatform.user.exception.UserAlreadyExistsException if email taken
     */
    AuthResponse register(RegisterRequest request, HttpServletRequest httpRequest);

    /**
     * Authenticates credentials and returns an access token.
     * The refresh token is set as an HttpOnly cookie by the controller.
     *
     * @param request  login payload
     * @param httpRequest the HTTP request (used to extract IP and User-Agent)
     * @return auth response containing user profile and access token
     * @throws com.interviewplatform.auth.exception.InvalidCredentialsException on bad credentials
     */
    AuthResponse login(LoginRequest request, HttpServletRequest httpRequest);

    /**
     * Validates the given refresh token hash, rotates it, and returns a new access token.
     * The new refresh token is set as an HttpOnly cookie by the controller.
     *
     * @param tokenHash  SHA-256 hash of the raw refresh token from the cookie
     * @param httpRequest the HTTP request (used to update IP and User-Agent)
     * @return new token pair (access token only; refresh token via cookie)
     */
    TokenPair refresh(String tokenHash, HttpServletRequest httpRequest);

    /**
     * Revokes the given refresh token (logout from current device).
     *
     * @param tokenHash SHA-256 hash of the raw refresh token from the cookie
     */
    void logout(String tokenHash);

    /**
     * Revokes ALL refresh tokens for the given user (logout from all devices).
     * Prepared for future "logout all devices" feature.
     *
     * @param userId the user's UUID as a string
     */
    void logoutAll(String userId);

    /**
     * Generates a raw refresh token string (UUID), persists its hash, and returns the raw value.
     *
     * @param userId      the user's UUID
     * @param httpRequest context for IP and User-Agent
     * @return the raw refresh token (to be delivered via HttpOnly cookie)
     */
    String generateAndStoreRefreshToken(java.util.UUID userId, HttpServletRequest httpRequest);
}
