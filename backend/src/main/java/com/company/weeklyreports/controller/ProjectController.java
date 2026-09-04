package com.company.weeklyreports.controller;

import com.company.weeklyreports.exception.ErrorResponse;
import com.company.weeklyreports.model.dto.CreateProjectRequest;
import com.company.weeklyreports.model.dto.ProjectDTO;
import com.company.weeklyreports.model.dto.UpdateProjectRequest;
import com.company.weeklyreports.model.entity.User;
import com.company.weeklyreports.security.CustomUserPrincipal;
import com.company.weeklyreports.service.ProjectService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * HTTP entry points for Project CRUD. Read is shared by both roles (team
 * members need the active project list to tag a report); every mutation is
 * manager-only.
 */
@RestController
@RequestMapping("/api/projects")
@Tag(name = "Projects", description = "Project CRUD; read is shared by both roles, mutations are manager-only")
public class ProjectController {

    private final ProjectService projectService;

    public ProjectController(ProjectService projectService) {
        this.projectService = projectService;
    }

    // GET /api/projects - deliberately has no @PreAuthorize. Per
    // rbac-matrix.md's "View Projects List" row, BOTH roles are allowed to
    // read this list (team members need it to populate the project
    // dropdown when creating a report) - there is no role to gate here at
    // all, unlike every other endpoint in this controller.
    @Operation(summary = "List all active projects")
    @GetMapping
    public List<ProjectDTO> listActiveProjects() {
        return projectService.listActiveProjects();
    }

    // POST /api/projects - manager-only per rbac-matrix.md ("Project /
    // Category Management (CRUD): Denied (Team Member) / Allowed
    // (Manager)"). This is a pure role check with no ownership dimension
    // (unlike Report endpoints) - project creation isn't scoped to "your
    // own" anything - so @PreAuthorize is the correct, sufficient gate.
    @Operation(summary = "Create a new project")
    @ApiResponse(responseCode = "400", description = "Validation failed",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "403", description = "Caller is not a MANAGER",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    @PostMapping
    @PreAuthorize("hasRole('MANAGER')")
    public ProjectDTO createProject(@Valid @RequestBody CreateProjectRequest request, Authentication authentication) {
        return projectService.createProject(request, currentUser(authentication));
    }

    // PATCH /api/projects/{id} - manager-only, same reasoning as create.
    @Operation(summary = "Edit a project's name/description")
    @ApiResponse(responseCode = "400", description = "Validation failed",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "403", description = "Caller is not a MANAGER",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "404", description = "Project not found",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    @PatchMapping("/{id}")
    @PreAuthorize("hasRole('MANAGER')")
    public ProjectDTO updateProject(@PathVariable Long id, @Valid @RequestBody UpdateProjectRequest request) {
        return projectService.updateProject(id, request);
    }

    // DELETE /api/projects/{id} - manager-only. "Delete" here is always a
    // soft-delete (isActive=false) - see ProjectService.deactivateProject
    // for why a hard delete would break every report that references this
    // project.
    @Operation(summary = "Soft-delete a project by setting isActive to false")
    @ApiResponse(responseCode = "403", description = "Caller is not a MANAGER",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "404", description = "Project not found",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('MANAGER')")
    public ProjectDTO deactivateProject(@PathVariable Long id) {
        return projectService.deactivateProject(id);
    }

    // Same mechanism as ReportController.currentUser() - see there for the
    // full explanation.
    private User currentUser(Authentication authentication) {
        return ((CustomUserPrincipal) authentication.getPrincipal()).getUser();
    }
}
