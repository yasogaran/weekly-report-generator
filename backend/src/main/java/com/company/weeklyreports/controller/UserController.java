package com.company.weeklyreports.controller;

import com.company.weeklyreports.model.dto.RoleChangeRequest;
import com.company.weeklyreports.model.dto.UserDTO;
import com.company.weeklyreports.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * MANAGER-only across the board (api-doc.md) — a flat role check with no ownership
 * dimension, so @PreAuthorize on every method here is sufficient; no service-layer guard
 * needed the way report ownership requires one.
 * <p>
 * Note: nothing here stops a manager from changing their own role or deactivating their own
 * account by calling these endpoints directly (the frontend UI hides that option, but that's
 * UX only — see components' own comments). Flagged as a backend-hardening item, not fixed
 * here, since it's not in the documented contract and the blast radius is self-inflicted.
 */
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@PreAuthorize("hasRole('MANAGER')")
public class UserController {

    private final UserService userService;

    @GetMapping
    public Page<UserDTO> getUsers(Pageable pageable) {
        return userService.getUsers(pageable);
    }

    @PatchMapping("/{id}/role")
    public UserDTO changeRole(@PathVariable Long id, @Valid @RequestBody RoleChangeRequest request) {
        return userService.changeRole(id, request.getRole());
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deactivateUser(@PathVariable Long id) {
        userService.deactivate(id);
    }
}
