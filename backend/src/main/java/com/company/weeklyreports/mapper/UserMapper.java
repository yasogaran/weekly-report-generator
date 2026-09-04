package com.company.weeklyreports.mapper;

import com.company.weeklyreports.model.dto.UserDTO;
import com.company.weeklyreports.model.entity.User;
import org.springframework.stereotype.Component;

/**
 * Entity <-> DTO conversion for User, kept out of the service layer per CLAUDE.md's mapper
 * pattern. There is only ever an entity -> DTO direction here: passwordHash simply has no
 * corresponding field on UserDTO to map into, which is what makes "never expose the hash"
 * structural rather than something a service method has to remember to omit.
 */
@Component
public class UserMapper {

    public UserDTO toDto(User user) {
        return UserDTO.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .role(user.getRole())
                .isActive(user.isActive())
                .createdAt(user.getCreatedAt())
                .build();
    }
}
