package com.company.weeklyreports.service;

import com.company.weeklyreports.exception.ValidationException;
import com.company.weeklyreports.mapper.UserMapper;
import com.company.weeklyreports.model.dto.AuthResponse;
import com.company.weeklyreports.model.dto.LoginRequest;
import com.company.weeklyreports.model.dto.RegisterRequest;
import com.company.weeklyreports.model.entity.Role;
import com.company.weeklyreports.model.entity.User;
import com.company.weeklyreports.repository.UserRepository;
import com.company.weeklyreports.security.CustomUserPrincipal;
import com.company.weeklyreports.security.JwtTokenProvider;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Registration and login logic. Plain concrete class, no interface - like
 * ProjectService/UserService, this is straightforward enough (no
 * versioning, no state machine) that Dependency Inversion would add file
 * overhead with no real benefit; that pattern is reserved for
 * ReportService/ReviewService per CLAUDE.md's design decision.
 */
@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthenticationManager authenticationManager;

    public AuthService(UserRepository userRepository,
                        PasswordEncoder passwordEncoder,
                        JwtTokenProvider jwtTokenProvider,
                        AuthenticationManager authenticationManager) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
        this.authenticationManager = authenticationManager;
    }

    // Creates a new account. Role is always TEAM_MEMBER here, full stop -
    // RegisterRequest has no role field at all (see model/dto/RegisterRequest),
    // so there's nothing a client could even send to influence it; assigning
    // MANAGER happens only through the future User Management task, never
    // through self-service registration, per rbac-matrix.md.
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ValidationException("Email is already registered: " + request.getEmail());
        }

        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .role(Role.TEAM_MEMBER)
                .build();

        User saved = userRepository.save(user);
        String token = jwtTokenProvider.generateToken(saved);
        return AuthResponse.builder().token(token).user(UserMapper.toDto(saved)).build();
    }

    // Verifies credentials via AuthenticationManager (which delegates to
    // CustomUserDetailsService + PasswordEncoder under the hood). A bad
    // email/password throws AuthenticationException/BadCredentialsException
    // straight out of .authenticate() - deliberately left unhandled here;
    // GlobalExceptionHandler (a separate task) will map it, not this method.
    public AuthResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));

        User user = ((CustomUserPrincipal) authentication.getPrincipal()).getUser();
        String token = jwtTokenProvider.generateToken(user);
        return AuthResponse.builder().token(token).user(UserMapper.toDto(user)).build();
    }
}
