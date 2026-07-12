package com.interviewplatform.user.repository;

import com.interviewplatform.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data JPA repository for {@link User} entities.
 *
 * <p><b>Sprint 2 — Authentication & User Management</b></p>
 */
@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    /**
     * Finds a user by their email address.
     * Used by Spring Security's {@code UserDetailsService} and the auth service.
     *
     * @param email the email address (case-sensitive, as stored)
     * @return an {@link Optional} containing the user, or empty if not found
     */
    Optional<User> findByEmail(String email);

    /**
     * Checks whether a user with the given email already exists.
     * Used for duplicate-email validation before attempting insertion.
     *
     * @param email the email address to check
     * @return {@code true} if an account with this email exists
     */
    boolean existsByEmail(String email);
}
