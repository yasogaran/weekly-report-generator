package com.company.weeklyreports.mapper;

import com.company.weeklyreports.model.dto.UserDTO;
import com.company.weeklyreports.model.entity.User;

/**
 * Converts between the User entity and its public DTO. Written by hand
 * (no MapStruct) so every field mapping is explicit and easy to explain.
 */
public class UserMapper {

    private UserMapper() {
    }

    // Deliberately omits passwordHash - this is the one thing this mapper
    // exists to enforce, so the entity's hash never reaches the API layer.
    public static UserDTO toDto(User user) {
        if (user == null) {
            return null;
        }
        return UserDTO.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .role(user.getRole())
                .createdAt(user.getCreatedAt())
                .build();
    }
}
