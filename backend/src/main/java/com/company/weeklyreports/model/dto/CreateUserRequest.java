package com.company.weeklyreports.model.dto;

import com.company.weeklyreports.model.entity.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/**
 * POST /users body (api-doc.md). Unlike the old self-service register flow, `role` IS read
 * from the request here — a manager is trusted to pick either valid role for the account
 * they're creating (@NotNull + the Role enum itself is what rejects anything else).
 */
@Getter
@Setter
public class CreateUserRequest {

    @NotBlank(message = "is required")
    private String name;

    @NotBlank(message = "is required")
    @Email(message = "must be a well-formed email address")
    private String email;

    @NotBlank(message = "is required")
    @Size(min = 8, message = "must be at least 8 characters")
    private String password;

    @NotNull(message = "is required")
    private Role role;
}
