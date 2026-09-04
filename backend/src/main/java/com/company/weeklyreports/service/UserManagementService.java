package com.company.weeklyreports.service;

import com.company.weeklyreports.exception.ResourceNotFoundException;
import com.company.weeklyreports.mapper.UserMapper;
import com.company.weeklyreports.model.dto.UserDTO;
import com.company.weeklyreports.model.entity.Role;
import com.company.weeklyreports.model.entity.User;
import com.company.weeklyreports.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Manager-only user administration: listing accounts, changing roles, and
 * deactivating accounts. Plain concrete class, no interface - same
 * reasoning as ProjectService/AuthService: this is simple CRUD-shaped
 * logic with no state machine or versioning worth demonstrating Dependency
 * Inversion on.
 */
@Service
public class UserManagementService {

    private final UserRepository userRepository;

    public UserManagementService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public Page<UserDTO> listUsers(Pageable pageable) {
        return userRepository.findAll(pageable).map(UserMapper::toDto);
    }

    // Role reassignment is the one piece of role management rbac-matrix.md
    // grants to managers ("User Management & Role Assignment: Allowed
    // (Manager)"). The new role's validity (must be TEAM_MEMBER or MANAGER)
    // is already guaranteed before this method runs - see
    // UpdateUserRoleRequest's comment on why an invalid string can't even
    // deserialize into the Role enum in the first place.
    @Transactional
    public UserDTO changeRole(Long userId, Role newRole) {
        User user = findUserOrThrow(userId);
        user.setRole(newRole);
        return UserMapper.toDto(userRepository.save(user));
    }

    // Soft-delete only: Report.user and ReviewAction.reviewer both FK to
    // this row. A hard delete would either violate those FK constraints or
    // (if cascading were ever added) destroy a former team member's entire
    // report history, or a former manager's entire review history - both
    // of which must survive the person leaving. Flipping isActive off also
    // blocks further login/API access for this account - see
    // CustomUserPrincipal.isEnabled() and JwtAuthFilter, which were
    // extended alongside this task specifically so deactivation takes
    // effect immediately rather than only once - and if - the account's
    // last-issued token expires.
    @Transactional
    public UserDTO deactivateUser(Long userId) {
        User user = findUserOrThrow(userId);
        user.setActive(false);
        return UserMapper.toDto(userRepository.save(user));
    }

    private User findUserOrThrow(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));
    }
}
