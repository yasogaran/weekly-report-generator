package com.company.weeklyreports.service;

import com.company.weeklyreports.mapper.UserMapper;
import com.company.weeklyreports.model.dto.AuthResponse;
import com.company.weeklyreports.model.dto.LoginRequest;
import com.company.weeklyreports.model.entity.User;
import com.company.weeklyreports.repository.UserRepository;
import com.company.weeklyreports.security.JwtTokenProvider;
import com.company.weeklyreports.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;

/**
 * Plain concrete class, not an interface + Impl — login is simple, linear logic with one
 * real implementation ever needed (CLAUDE.md's Dependency Inversion note reserves the
 * interface+Impl split for ReportService/ReviewService specifically). Account creation lives
 * in UserManagementService now — there is no self-service registration (docs/api/api-doc.md).
 */
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthenticationManager authenticationManager;
    private final UserMapper userMapper;

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
