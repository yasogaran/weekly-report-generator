package com.company.weeklyreports.service;

import com.company.weeklyreports.exception.ValidationException;
import com.company.weeklyreports.mapper.UserMapper;
import com.company.weeklyreports.model.dto.AuthResponse;
import com.company.weeklyreports.model.dto.LoginRequest;
import com.company.weeklyreports.model.dto.RegisterRequest;
import com.company.weeklyreports.model.entity.Role;
import com.company.weeklyreports.model.entity.User;
import com.company.weeklyreports.repository.UserRepository;
import com.company.weeklyreports.security.JwtTokenProvider;
import com.company.weeklyreports.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Plain concrete class, not an interface + Impl — register/login is simple, linear logic
 * with one real implementation ever needed, so an interface here would be file overhead with
 * no benefit (CLAUDE.md's Dependency Inversion note reserves the interface+Impl split for
 * ReportService/ReviewService specifically).
 */
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthenticationManager authenticationManager;
    private final UserMapper userMapper;

    /**
     * Creates a new TEAM_MEMBER account. `role` is never read from the request (api-doc.md —
     * self-registration is always TEAM_MEMBER; MANAGER accounts only come from seed data or
     * future admin promotion via PATCH /users/{id}/role).
     */
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ValidationException("An account with this email already exists.");
        }

        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .role(Role.TEAM_MEMBER)
                .build();
        User saved = userRepository.save(user);

        String token = jwtTokenProvider.generateToken(new UserPrincipal(saved));
        return new AuthResponse(token, userMapper.toDto(saved));
    }

    /**
     * Delegates the actual credential check to Spring Security's AuthenticationManager
     * (CustomUserDetailsService + BCrypt comparison) rather than comparing hashes by hand —
     * that's also what makes a deactivated account (isEnabled() == false) fail here as a
     * DisabledException automatically, without a separate isActive check in this method.
     */
    public AuthResponse login(LoginRequest request) {
        var authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));

        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        User user = userRepository.findByEmail(principal.getEmail())
                .orElseThrow(() -> new IllegalStateException("Authenticated user vanished mid-request"));

        String token = jwtTokenProvider.generateToken(principal);
        return new AuthResponse(token, userMapper.toDto(user));
    }
}
