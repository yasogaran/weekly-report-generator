package com.company.weeklyreports.controller;

import com.company.weeklyreports.model.dto.ProjectDTO;
import com.company.weeklyreports.model.dto.ProjectRequest;
import com.company.weeklyreports.service.ProjectService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * GET has no role restriction at all (any authenticated user needs the project list to
 * populate the report form's dropdown, api-doc.md) — every mutating endpoint is
 * MANAGER-only, enforced with @PreAuthorize (a flat role check, no ownership dimension, so
 * @PreAuthorize is the right tool here rather than a service-layer ownership guard).
 */
@RestController
@RequestMapping("/projects")
@RequiredArgsConstructor
public class ProjectController {

    private final ProjectService projectService;

    @GetMapping
    public List<ProjectDTO> getProjects() {
        return projectService.getActiveProjects();
    }

    @PostMapping
    @PreAuthorize("hasRole('MANAGER')")
    public ProjectDTO createProject(@Valid @RequestBody ProjectRequest request) {
        return projectService.create(request);
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasRole('MANAGER')")
    public ProjectDTO updateProject(@PathVariable Long id, @Valid @RequestBody ProjectRequest request) {
        return projectService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('MANAGER')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteProject(@PathVariable Long id) {
        projectService.deactivate(id);
    }
}
