package com.interviewplatform.user.repository;

import com.interviewplatform.user.entity.RefreshToken;
import com.interviewplatform.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data JPA repository for {@link RefreshToken} entities.
 *
 * <p><b>Sprint 2 — Authentication & User Management</b></p>
 */
@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, UUID> {

    /**
     * Finds an active (non-revoked, non-expired) refresh token by its hash.
     *
     * @param tokenHash SHA-256 hash of the raw refresh token
     * @return the matching token if found and usable
     */
    Optional<RefreshToken> findByTokenHashAndRevokedFalse(String tokenHash);

    /**
     * Finds all active tokens for a user.
     * Used for "logout all devices" — revoke all non-revoked tokens.
     *
     * @param user the owning user
     * @return list of all non-revoked tokens
     */
    List<RefreshToken> findByUserAndRevokedFalse(User user);

    /**
     * Bulk-revokes all active tokens for a user with a given reason.
     * Prepared for "logout from all devices" feature.
     *
     * @param userId the user's UUID
     * @param reason revocation reason (e.g. "LOGOUT_ALL")
     * @param revokedAt timestamp of revocation
     */
    @Modifying
    @Query("""
            UPDATE RefreshToken rt
            SET rt.revoked = true,
                rt.revokedAt = :revokedAt,
                rt.reason = :reason
            WHERE rt.user.id = :userId
              AND rt.revoked = false
            """)
    void revokeAllByUserId(
            @Param("userId") UUID userId,
            @Param("reason") String reason,
            @Param("revokedAt") Instant revokedAt
    );

    /**
     * Deletes expired tokens for housekeeping.
     * Called by a scheduled task (future sprint).
     *
     * @param now current time; tokens with expiresAt before this are deleted
     */
    @Modifying
    @Query("DELETE FROM RefreshToken rt WHERE rt.expiresAt < :now")
    void deleteExpiredTokens(@Param("now") Instant now);
}
