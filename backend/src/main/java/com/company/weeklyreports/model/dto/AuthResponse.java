package com.company.weeklyreports.model.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

/** Response shape for POST /auth/login. */
@Getter
@AllArgsConstructor
public class AuthResponse {
    private String token;
    private UserDTO user;
}
