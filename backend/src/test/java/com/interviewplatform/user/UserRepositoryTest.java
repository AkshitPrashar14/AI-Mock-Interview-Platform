package com.interviewplatform.user;

import com.interviewplatform.user.entity.Role;
import com.interviewplatform.user.entity.User;
import com.interviewplatform.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * JPA slice test for {@link UserRepository}.
 *
 * <p>Uses {@code @DataJpaTest} — loads only JPA layer, no full Spring context.
 * Requires a database connection (uses the dev profile PostgreSQL).</p>
 *
 * <p><b>Sprint 2 — Authentication & User Management</b></p>
 */
@DataJpaTest
@ActiveProfiles("dev")
@DisplayName("UserRepository Slice Tests")
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    @DisplayName("findByEmail returns user when email exists")
    void findByEmail_found() {
        User user = userRepository.save(User.builder()
                .firstName("Jane")
                .lastName("Doe")
                .email("jane@example.com")
                .passwordHash("$2a$12$hash")
                .role(Role.USER)
                .isActive(true)
                .isEmailVerified(false)
                .build());

        Optional<User> found = userRepository.findByEmail("jane@example.com");

        assertThat(found).isPresent();
        assertThat(found.get().getId()).isEqualTo(user.getId());
    }

    @Test
    @DisplayName("findByEmail returns empty when email not found")
    void findByEmail_notFound() {
        Optional<User> found = userRepository.findByEmail("nonexistent@example.com");
        assertThat(found).isEmpty();
    }

    @Test
    @DisplayName("existsByEmail returns true when email exists")
    void existsByEmail_true() {
        userRepository.save(User.builder()
                .firstName("John")
                .lastName("Smith")
                .email("john@example.com")
                .passwordHash("$2a$12$hash")
                .role(Role.USER)
                .isActive(true)
                .isEmailVerified(false)
                .build());

        assertThat(userRepository.existsByEmail("john@example.com")).isTrue();
    }

    @Test
    @DisplayName("existsByEmail returns false when email not found")
    void existsByEmail_false() {
        assertThat(userRepository.existsByEmail("nobody@example.com")).isFalse();
    }
}
