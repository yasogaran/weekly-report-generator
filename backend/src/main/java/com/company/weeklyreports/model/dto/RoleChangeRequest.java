package com.company.weeklyreports.model.dto;

import com.company.weeklyreports.model.entity.Role;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

/** PATCH /users/{id}/role body (api-doc.md). */
@Getter
@Setter
public class RoleChangeRequest {

    @NotNull(message = "is required")
    private Role role;
}
