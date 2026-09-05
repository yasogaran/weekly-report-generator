package com.company.weeklyreports.service;

import com.company.weeklyreports.exception.ResourceNotFoundException;
import com.company.weeklyreports.exception.ValidationException;
import com.company.weeklyreports.mapper.UserMapper;
import com.company.weeklyreports.model.dto.CreateUserRequest;
import com.company.weeklyreports.model.dto.UserDTO;
import com.company.weeklyreports.model.entity.Role;
import com.company.weeklyreports.model.entity.User;
import com.company.weeklyreports.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Plain concrete class, no interface — same reasoning as ProjectService (CLAUDE.md reserves
 * interface+Impl for ReportService/ReviewService).
 */
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    /** GET /users — paginated, MANAGER only (enforced in UserController), all users regardless of isActive. */
    public Page<UserDTO> getUsers(Pageable pageable) {
        return userRepository.findAll(pageable).map(userMapper::toDto);
    }

    /**
     * POST /users — the only way any account (TEAM_MEMBER or MANAGER) gets created now that
     * self-service registration is gone (api-doc.md). MANAGER-only, enforced in
     * UserController. Reuses the same duplicate-email check the old register flow used, and
     * hashes the password the same way — the only real difference is `role` comes from the
     * request instead of being hardcoded.
     */
    @Transactional
    public UserDTO createUser(CreateUserRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ValidationException("An account with this email already exists.");
        }

        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .role(request.getRole())
                .build();
        return userMapper.toDto(userRepository.save(user));
    }

    @Transactional
    public UserDTO changeRole(Long id, Role newRole) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + id));
        user.setRole(newRole);
        return userMapper.toDto(userRepository.save(user));
    }

    /**
     * Soft delete (api-doc.md): sets isActive=false, blocks future login (UserPrincipal.isEnabled())
     * and invalidates every currently-authenticated request on its next use (JwtAuthFilter
     * re-checks isActive fresh from the DB every time, not from token claims). Existing
     * reports/reviews this user authored are left completely untouched — no cascade.
     */
    @Transactional
    public void deactivate(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + id));
        user.setActive(false);
        userRepository.save(user);
    }
}
