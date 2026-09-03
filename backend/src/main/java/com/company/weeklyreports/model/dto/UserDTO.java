package com.company.weeklyreports.model.dto;

import com.company.weeklyreports.model.entity.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Public-facing view of a User. Deliberately excludes passwordHash - this
 * is the shape returned by every endpoint that surfaces user info (auth,
 * user management, report/review "who did this" fields).
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserDTO {

    private Long id;

    private String name;

    private String email;

    private Role role;

    private LocalDateTime createdAt;
}
