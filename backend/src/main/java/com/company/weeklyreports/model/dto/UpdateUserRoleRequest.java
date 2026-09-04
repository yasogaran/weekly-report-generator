package com.company.weeklyreports.model.dto;

import com.company.weeklyreports.model.entity.Role;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Payload for PATCH /users/{id}/role (manager only). Reuses the Role enum
 * directly rather than a String field - Jackson will reject any value that
 * isn't exactly TEAM_MEMBER or MANAGER at deserialization time, so "one of
 * the two valid values" is enforced by the type system itself, before this
 * even reaches a validation annotation or the service.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateUserRoleRequest {

    @NotNull
    private Role role;
}
