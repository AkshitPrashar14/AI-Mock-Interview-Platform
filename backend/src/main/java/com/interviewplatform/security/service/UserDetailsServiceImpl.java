package com.interviewplatform.security.service;

import com.interviewplatform.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Spring Security {@link UserDetailsService} implementation.
 *
 * <p>Loads a {@link com.interviewplatform.user.entity.User} entity by email address.
 * Spring Security uses email as the "username" throughout the platform.</p>
 *
 * <p><b>Sprint 2 — Authentication & User Management</b></p>
 */
@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    /**
     * Loads the user by email for Spring Security authentication.
     *
     * <p>The returned {@link UserDetails} is the {@code User} entity itself,
     * which implements {@link UserDetails} directly.</p>
     *
     * @param email the email address (used as username)
     * @return the {@link UserDetails} object
     * @throws UsernameNotFoundException if no user with this email exists
     */
    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "User not found with email: " + email));
    }
}
