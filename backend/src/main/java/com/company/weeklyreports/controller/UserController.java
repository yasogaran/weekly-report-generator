package com.company.weeklyreports.controller;

import com.company.weeklyreports.exception.ErrorResponse;
import com.company.weeklyreports.model.dto.UpdateUserRoleRequest;
import com.company.weeklyreports.model.dto.UserDTO;
import com.company.weeklyreports.service.UserManagementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * HTTP entry points for manager-only user administration. Every endpoint
 * here is manager-only per rbac-matrix.md's "User Management & Role
 * Assignment" row - unlike ProjectController's read endpoint, there is no
 * read-only carve-out for team members anywhere in this controller.
 */
@RestController
@RequestMapping("/api/users")
@PreAuthorize("hasRole('MANAGER')")
@Tag(name = "Users", description = "Manager-only user administration")
public class UserController {

    private final UserManagementService userManagementService;

    public UserController(UserManagementService userManagementService) {
        this.userManagementService = userManagementService;
    }

    // GET /api/users - paginated per the assignment spec's "pagination on
    // any list-returning endpoint" requirement, using Spring's built-in
    // Pageable resolver (page/size/sort query params).
    @Operation(summary = "List all users, paginated")
    @ApiResponse(responseCode = "403", description = "Caller is not a MANAGER",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    @GetMapping
    public Page<UserDTO> listUsers(Pageable pageable) {
        return userManagementService.listUsers(pageable);
    }

    // PATCH /api/users/{id}/role - reassigns a user's role.
    @Operation(summary = "Reassign a user's role")
    @ApiResponse(responseCode = "400", description = "Role is missing or not one of TEAM_MEMBER/MANAGER",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "403", description = "Caller is not a MANAGER",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "404", description = "User not found",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    @PatchMapping("/{id}/role")
    public UserDTO changeRole(@PathVariable Long id, @Valid @RequestBody UpdateUserRoleRequest request) {
        return userManagementService.changeRole(id, request.getRole());
    }

    // DELETE /api/users/{id} - deactivates (never hard-deletes) an account.
    // See UserManagementService.deactivateUser for why a real row delete
    // would orphan the user's report/review history.
    @Operation(summary = "Soft-delete a user by setting isActive to false")
    @ApiResponse(responseCode = "403", description = "Caller is not a MANAGER",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "404", description = "User not found",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    @DeleteMapping("/{id}")
    public UserDTO deactivateUser(@PathVariable Long id) {
        return userManagementService.deactivateUser(id);
    }
}
