package com.interviewplatform.user.mapper;

import com.interviewplatform.user.dto.response.UserResponse;
import com.interviewplatform.user.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * MapStruct mapper for converting {@link User} entity to {@link UserResponse} DTO.
 *
 * <p>All entity→DTO conversion must go through this mapper.
 * Controllers must never manually construct DTOs from entities.</p>
 *
 * <p><b>Sprint 2 — Authentication & User Management</b></p>
 */
@Mapper(componentModel = "spring")
public interface UserMapper {

    /**
     * Converts a {@link User} entity to a {@link UserResponse} DTO.
     *
     * <p>The {@code passwordHash} field is intentionally excluded from
     * the mapping target — it must never appear in any API response.</p>
     *
     * @param user the source entity
     * @return the mapped response DTO
     */
    @Mapping(target = "isActive", source = "active")
    @Mapping(target = "isEmailVerified", source = "emailVerified")
    UserResponse toResponse(User user);
}
