package com.company.weeklyreports.model.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

/** POST /auth/login body (api-doc.md). No @Email check here — a malformed email should just fail as "not found" (401), not get its own distinct 400. */
@Getter
@Setter
public class LoginRequest {

    @NotBlank(message = "is required")
    private String email;

    @NotBlank(message = "is required")
    private String password;
}
