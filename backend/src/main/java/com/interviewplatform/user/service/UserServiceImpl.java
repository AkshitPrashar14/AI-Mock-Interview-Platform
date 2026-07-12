package com.interviewplatform.user.service;

import com.interviewplatform.user.dto.request.UpdateProfileRequest;
import com.interviewplatform.user.dto.response.UserResponse;
import com.interviewplatform.user.entity.User;
import com.interviewplatform.user.exception.UserNotFoundException;
import com.interviewplatform.user.mapper.UserMapper;
import com.interviewplatform.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Default implementation of {@link UserService}.
 *
 * <p><b>Sprint 2 — Authentication & User Management</b></p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Override
    @Transactional(readOnly = true)
    public UserResponse findById(UUID id) {
        User user = findEntityById(id);
        return userMapper.toResponse(user);
    }

    @Override
    @Transactional(readOnly = true)
    public User findEntityById(UUID id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getCurrentUser(UUID userId) {
        return findById(userId);
    }

    @Override
    @Transactional
    public UserResponse updateProfile(UUID userId, UpdateProfileRequest request) {
        User user = findEntityById(userId);

        if (request.getFirstName() != null) {
            user.setFirstName(request.getFirstName());
        }
        if (request.getLastName() != null) {
            user.setLastName(request.getLastName());
        }
        if (request.getProfilePictureUrl() != null) {
            user.setProfilePictureUrl(request.getProfilePictureUrl());
        }

        User saved = userRepository.save(user);
        log.debug("Profile updated for user: {}", userId);
        return userMapper.toResponse(saved);
    }
}
