package com.company.weeklyreports.controller;

import com.company.weeklyreports.exception.ErrorResponse;
import com.company.weeklyreports.model.dto.AuthResponse;
import com.company.weeklyreports.model.dto.LoginRequest;
import com.company.weeklyreports.model.dto.RegisterRequest;
import com.company.weeklyreports.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * HTTP entry points for registration and login. Both endpoints are
 * permitAll per SecurityConfig (/api/auth/**), since a caller has no token
 * yet when hitting either of them.
 *
 * There is deliberately no POST /logout here: JWTs are stateless, so there
 * is nothing server-side to invalidate without introducing a token
 * blocklist (out of scope for this build). This is a known, accepted
 * limitation, not a missed requirement - the frontend simply discards the
 * token client-side, and the token expires naturally per
 * app.jwt.expiration-ms.
 */
@RestController
@RequestMapping("/api/auth")
@Tag(name = "Auth", description = "Registration and login")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @Operation(summary = "Register a new account; always assigned the TEAM_MEMBER role")
    @ApiResponse(responseCode = "400", description = "Validation failed, or the email is already registered",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    @PostMapping("/register")
    public AuthResponse register(@Valid @RequestBody RegisterRequest request) {
        return authService.register(request);
    }

    @Operation(summary = "Authenticate with email/password and receive a JWT")
    @ApiResponse(responseCode = "401", description = "Invalid email or password",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }
}
