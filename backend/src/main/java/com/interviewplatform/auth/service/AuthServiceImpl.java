package com.interviewplatform.auth.service;

import com.interviewplatform.auth.dto.request.LoginRequest;
import com.interviewplatform.auth.dto.request.RegisterRequest;
import com.interviewplatform.auth.dto.response.AuthResponse;
import com.interviewplatform.auth.dto.response.TokenPair;
import com.interviewplatform.auth.exception.InvalidCredentialsException;
import com.interviewplatform.auth.exception.RefreshTokenExpiredException;
import com.interviewplatform.auth.exception.RefreshTokenNotFoundException;
import com.interviewplatform.security.jwt.JwtProperties;
import com.interviewplatform.security.jwt.JwtService;
import com.interviewplatform.user.entity.RefreshToken;
import com.interviewplatform.user.entity.Role;
import com.interviewplatform.user.entity.User;
import com.interviewplatform.user.exception.UserAlreadyExistsException;
import com.interviewplatform.user.mapper.UserMapper;
import com.interviewplatform.user.repository.RefreshTokenRepository;
import com.interviewplatform.user.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HexFormat;
import java.util.UUID;

/**
 * Default implementation of {@link AuthService}.
 *
 * <p><b>Security logging rules enforced:</b></p>
 * <ul>
 *   <li>NEVER logs passwords (hashed or raw)</li>
 *   <li>NEVER logs JWT tokens</li>
 *   <li>NEVER logs refresh token values</li>
 *   <li>Logs only: user ID, email (debug), and operation name</li>
 * </ul>
 *
 * <p><b>Sprint 2 — Authentication & User Management</b></p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final JwtProperties jwtProperties;
    private final UserMapper userMapper;

    // =========================================================================
    // Register
    // =========================================================================

    @Override
    @Transactional
    public AuthResponse register(RegisterRequest request, HttpServletRequest httpRequest) {
        // Service-layer email uniqueness check (defence-in-depth; DB constraint is backup)
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new UserAlreadyExistsException(request.getEmail());
        }

        User user = User.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .role(Role.USER)
                .isActive(true)
                .isEmailVerified(false)
                .build();

        user = userRepository.save(user);
        log.info("New user registered: id={}", user.getId());

        // TODO [Event]: publish UserRegisteredEvent(user.getId(), user.getEmail())

        String accessToken = jwtService.generateAccessToken(user);
        return AuthResponse.builder()
                .user(userMapper.toResponse(user))
                .accessToken(accessToken)
                .expiresIn(jwtProperties.getAccessTokenExpirationMs() / 1000)
                .build();
    }

    // =========================================================================
    // Login
    // =========================================================================

    @Override
    @Transactional
    public AuthResponse login(LoginRequest request, HttpServletRequest httpRequest) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(InvalidCredentialsException::new);

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            log.debug("Failed login attempt for user: {}", user.getId());
            throw new InvalidCredentialsException();
        }

        // Update last login time
        user.setLastLoginAt(Instant.now());
        user.setFailedLoginAttempts(0);
        userRepository.save(user);

        log.info("User logged in: id={}", user.getId());

        // TODO [Event]: publish UserLoggedInEvent(user.getId(), extractIp(httpRequest))

        String accessToken = jwtService.generateAccessToken(user);
        return AuthResponse.builder()
                .user(userMapper.toResponse(user))
                .accessToken(accessToken)
                .expiresIn(jwtProperties.getAccessTokenExpirationMs() / 1000)
                .build();
    }

    // =========================================================================
    // Refresh
    // =========================================================================

    @Override
    @Transactional
    public TokenPair refresh(String rawToken, HttpServletRequest httpRequest) {
        String tokenHash = hashToken(rawToken);

        RefreshToken stored = refreshTokenRepository
                .findByTokenHashAndRevokedFalse(tokenHash)
                .orElseThrow(RefreshTokenNotFoundException::new);

        if (stored.isExpired()) {
            // Revoke it to clean up
            stored.setRevoked(true);
            stored.setRevokedAt(Instant.now());
            stored.setReason("EXPIRED");
            refreshTokenRepository.save(stored);
            throw new RefreshTokenExpiredException();
        }

        // Revoke old token (rotation)
        stored.setRevoked(true);
        stored.setRevokedAt(Instant.now());
        stored.setReason("ROTATED");
        stored.setLastUsedAt(Instant.now());
        refreshTokenRepository.save(stored);

        User user = stored.getUser();
        String accessToken = jwtService.generateAccessToken(user);

        return TokenPair.builder()
                .accessToken(accessToken)
                .expiresIn(jwtProperties.getAccessTokenExpirationMs() / 1000)
                .build();
    }

    // =========================================================================
    // Logout
    // =========================================================================

    @Override
    @Transactional
    public void logout(String rawToken) {
        String tokenHash = hashToken(rawToken);

        refreshTokenRepository.findByTokenHashAndRevokedFalse(tokenHash)
                .ifPresent(token -> {
                    token.setRevoked(true);
                    token.setRevokedAt(Instant.now());
                    token.setReason("LOGOUT");
                    refreshTokenRepository.save(token);
                    log.info("Refresh token revoked for user: {}", token.getUser().getId());
                    // TODO [Event]: publish UserLoggedOutEvent(token.getUser().getId(), token.getDeviceId())
                });
    }

    @Override
    @Transactional
    public void logoutAll(String userId) {
        // TODO [Future]: Logout from all devices
        // refreshTokenRepository.revokeAllByUserId(UUID.fromString(userId), "LOGOUT_ALL", Instant.now());
        // TODO [Event]: publish UserLoggedOutEvent(userId, deviceId=null)
        log.info("Logout-all requested for user: {} (not yet active in V1)", userId);
    }

    // =========================================================================
    // Refresh token persistence
    // =========================================================================

    @Override
    @Transactional
    public String generateAndStoreRefreshToken(UUID userId, HttpServletRequest httpRequest) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalStateException("User not found during token generation: " + userId));

        String rawToken = UUID.randomUUID().toString();
        String tokenHash = hashToken(rawToken);

        RefreshToken refreshToken = RefreshToken.builder()
                .user(user)
                .tokenHash(tokenHash)
                .ipAddress(extractIp(httpRequest))
                .userAgent(extractUserAgent(httpRequest))
                .expiresAt(Instant.now().plus(jwtProperties.getRefreshTokenExpirationDays(), ChronoUnit.DAYS))
                .build();

        refreshTokenRepository.save(refreshToken);
        return rawToken; // raw value returned to controller for cookie delivery
    }

    // =========================================================================
    // Private helpers
    // =========================================================================

    /**
     * Computes SHA-256 hash of a token string.
     * Only the hash is ever stored — the raw token is never persisted.
     */
    private String hashToken(String rawToken) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(rawToken.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }

    private String extractIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    private String extractUserAgent(HttpServletRequest request) {
        String ua = request.getHeader("User-Agent");
        return ua != null ? ua : "Unknown";
    }
}
