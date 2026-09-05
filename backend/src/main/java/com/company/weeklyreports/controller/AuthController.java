package com.company.weeklyreports.controller;

import com.company.weeklyreports.model.dto.AuthResponse;
import com.company.weeklyreports.model.dto.LoginRequest;
import com.company.weeklyreports.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Thin HTTP layer only — validates the request shape (@Valid) and delegates to AuthService.
 * Public per SecurityConfig ("/auth/login".permitAll()). There is deliberately no
 * self-service registration endpoint here (docs/api/api-doc.md) — every account is created
 * by a manager via POST /users (see UserController).
 */
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }
}
