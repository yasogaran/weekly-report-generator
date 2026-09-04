package com.company.weeklyreports.model.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/**
 * POST /auth/register body (api-doc.md). `role` is deliberately not a field here at all —
 * every self-registered account is hardcoded TEAM_MEMBER in AuthService, so there's nothing
 * for a client to even attempt to override.
 */
@Getter
@Setter
public class RegisterRequest {

    @NotBlank(message = "is required")
    private String name;

    @NotBlank(message = "is required")
    @Email(message = "must be a well-formed email address")
    private String email;

    @NotBlank(message = "is required")
    @Size(min = 8, message = "must be at least 8 characters")
    private String password;
}
