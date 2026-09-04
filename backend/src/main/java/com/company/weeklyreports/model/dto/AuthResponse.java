package com.company.weeklyreports.model.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

/** Response shape for both POST /auth/register and POST /auth/login (api-doc.md — identical shape). */
@Getter
@AllArgsConstructor
public class AuthResponse {
    private String token;
    private UserDTO user;
}
