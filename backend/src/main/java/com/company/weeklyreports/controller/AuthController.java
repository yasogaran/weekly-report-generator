package com.company.weeklyreports.controller;

import com.company.weeklyreports.model.dto.AuthResponse;
import com.company.weeklyreports.model.dto.LoginRequest;
import com.company.weeklyreports.model.dto.RegisterRequest;
import com.company.weeklyreports.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Thin HTTP layer only — both endpoints just validate the request shape (@Valid) and
 * delegate to AuthService for the actual logic, per CLAUDE.md's "controllers stay thin"
 * rule. Public per SecurityConfig ("/auth/**".permitAll()).
 */
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public AuthResponse register(@Valid @RequestBody RegisterRequest request) {
        return authService.register(request);
    }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }
}
