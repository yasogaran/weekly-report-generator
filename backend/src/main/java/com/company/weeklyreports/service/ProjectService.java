package com.company.weeklyreports.service;

import com.company.weeklyreports.exception.ResourceNotFoundException;
import com.company.weeklyreports.mapper.ProjectMapper;
import com.company.weeklyreports.model.dto.ProjectDTO;
import com.company.weeklyreports.model.dto.ProjectRequest;
import com.company.weeklyreports.model.entity.Project;
import com.company.weeklyreports.repository.ProjectRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Plain concrete class, no interface — simple CRUD with one implementation ever needed
 * (CLAUDE.md reserves the interface+Impl split for ReportService/ReviewService specifically).
 */
@Service
@RequiredArgsConstructor
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final ProjectMapper projectMapper;

    /** GET /projects — active projects only, per api-doc.md. */
    public List<ProjectDTO> getActiveProjects() {
        return projectRepository.findByIsActiveTrue().stream().map(projectMapper::toDto).toList();
    }

    @Transactional
    public ProjectDTO create(ProjectRequest request) {
        Project project = Project.builder()
                .name(request.getName())
                .description(request.getDescription())
                .build();
        return projectMapper.toDto(projectRepository.save(project));
    }

    @Transactional
    public ProjectDTO update(Long id, ProjectRequest request) {
        Project project = findActiveOrThrow(id);
        project.setName(request.getName());
        project.setDescription(request.getDescription());
        return projectMapper.toDto(projectRepository.save(project));
    }

    /**
     * Soft delete — sets isActive=false, never removes the row (api-doc.md): Reports
     * reference a project by FK, and hard-deleting would break their history.
     */
    @Transactional
    public void deactivate(Long id) {
        Project project = findActiveOrThrow(id);
        project.setActive(false);
        projectRepository.save(project);
    }

    private Project findActiveOrThrow(Long id) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found: " + id));
        if (!project.isActive()) {
            throw new ResourceNotFoundException("Project not found: " + id);
        }
        return project;
    }
}
