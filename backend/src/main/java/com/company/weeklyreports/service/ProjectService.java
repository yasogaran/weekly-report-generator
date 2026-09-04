package com.company.weeklyreports.service;

import com.company.weeklyreports.exception.ResourceNotFoundException;
import com.company.weeklyreports.mapper.ProjectMapper;
import com.company.weeklyreports.model.dto.CreateProjectRequest;
import com.company.weeklyreports.model.dto.ProjectDTO;
import com.company.weeklyreports.model.dto.UpdateProjectRequest;
import com.company.weeklyreports.model.entity.Project;
import com.company.weeklyreports.model.entity.User;
import com.company.weeklyreports.repository.ProjectRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * CRUD for Projects. Plain concrete class, no interface - simple CRUD with
 * no state machine or versioning, same reasoning as UserManagementService
 * and AuthService: Dependency Inversion is reserved for ReportService/
 * ReviewService per CLAUDE.md's design decision.
 */
@Service
public class ProjectService {

    private final ProjectRepository projectRepository;

    public ProjectService(ProjectRepository projectRepository) {
        this.projectRepository = projectRepository;
    }

    // Every active project, for the report-creation project picker (both
    // roles) and the manager's project-management screen.
    @Transactional(readOnly = true)
    public List<ProjectDTO> listActiveProjects() {
        return projectRepository.findByIsActiveTrue().stream()
                .map(ProjectMapper::toDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public ProjectDTO createProject(CreateProjectRequest request, User currentManager) {
        Project project = ProjectMapper.toEntity(request, currentManager);
        return ProjectMapper.toDto(projectRepository.save(project));
    }

    @Transactional
    public ProjectDTO updateProject(Long projectId, UpdateProjectRequest request) {
        Project existing = findProjectOrThrow(projectId);
        ProjectMapper.applyToEntity(existing, request);
        return ProjectMapper.toDto(projectRepository.save(existing));
    }

    // Soft-delete only: Report.project is a FK to this row, so a hard
    // delete would either fail on the FK constraint or (worse, if cascading
    // were ever misconfigured) silently destroy every report tagged with
    // this project. Flipping isActive off removes it from new-report
    // pickers while every existing report keeps a valid, readable
    // reference to it.
    @Transactional
    public ProjectDTO deactivateProject(Long projectId) {
        Project existing = findProjectOrThrow(projectId);
        existing.setActive(false);
        return ProjectMapper.toDto(projectRepository.save(existing));
    }

    private Project findProjectOrThrow(Long projectId) {
        return projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found: " + projectId));
    }
}
