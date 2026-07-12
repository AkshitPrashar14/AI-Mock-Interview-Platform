package com.interviewplatform.user.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.util.UUID;

/**
 * Refresh token entity.
 *
 * <p><b>Security model:</b></p>
 * <ul>
 *   <li>The raw token (UUID) is generated and returned to the client as an
 *       {@code HttpOnly} cookie — it is NEVER persisted.</li>
 *   <li>Only {@code tokenHash} (SHA-256 of the raw token) is stored in the DB.
 *       A database breach cannot leak valid refresh tokens.</li>
 * </ul>
 *
 * <p><b>Extended fields</b> (prepared for future sprints):</p>
 * <ul>
 *   <li>{@link #deviceId} / {@link #deviceName} — multi-device session management</li>
 *   <li>{@link #ipAddress} / {@link #userAgent} — login history and anomaly detection</li>
 *   <li>{@link #lastUsedAt} — idle session detection</li>
 *   <li>{@link #revokedAt} / {@link #reason} — audit trail for revocations</li>
 * </ul>
 *
 * <p><b>Sprint 2 — Authentication & User Management</b></p>
 */
@Entity
@Table(name = "refresh_tokens")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(nullable = false, updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /** SHA-256 hash of the raw refresh token. The raw token is never stored. */
    @Column(name = "token_hash", nullable = false, unique = true, length = 255)
    private String tokenHash;

    /**
     * Optional client-supplied device identifier.
     * Prepared for future "logout from this device" feature.
     */
    @Column(name = "device_id", length = 255)
    private String deviceId;

    /**
     * Optional human-readable device name (e.g. "Chrome on Windows").
     * Prepared for future device management UI.
     */
    @Column(name = "device_name", length = 255)
    private String deviceName;

    /** Client IP address at time of token issuance. */
    @Column(name = "ip_address", nullable = false, length = 45)
    private String ipAddress;

    /** User-Agent header at time of token issuance. */
    @Column(name = "user_agent", nullable = false)
    private String userAgent;

    /** Token expiry — absolute timestamp. */
    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    /** Updated each time this token is used to refresh. */
    @Column(name = "last_used_at")
    private Instant lastUsedAt;

    @Column(name = "revoked", nullable = false)
    @Builder.Default
    private boolean revoked = false;

    /** Set when {@link #revoked} becomes true. */
    @Column(name = "revoked_at")
    private Instant revokedAt;

    /**
     * Human-readable reason for revocation.
     * Expected values: LOGOUT, LOGOUT_ALL, EXPIRED, SECURITY_REVOKE.
     */
    @Column(name = "reason", length = 100)
    private String reason;

    // =========================================================================
    // Helpers
    // =========================================================================

    /** Returns true if this token has expired by absolute time, regardless of revoked flag. */
    public boolean isExpired() {
        return Instant.now().isAfter(expiresAt);
    }

    /** Convenience — true if the token is usable (not revoked and not expired). */
    public boolean isUsable() {
        return !revoked && !isExpired();
    }
}
