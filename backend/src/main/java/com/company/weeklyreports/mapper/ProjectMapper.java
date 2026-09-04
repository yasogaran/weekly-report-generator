package com.company.weeklyreports.mapper;

import com.company.weeklyreports.model.dto.CreateProjectRequest;
import com.company.weeklyreports.model.dto.ProjectDTO;
import com.company.weeklyreports.model.dto.UpdateProjectRequest;
import com.company.weeklyreports.model.entity.Project;
import com.company.weeklyreports.model.entity.User;

/**
 * Converts between the Project entity and its DTOs. Written by hand
 * (no MapStruct) so every field mapping is explicit and easy to explain.
 */
public class ProjectMapper {

    private ProjectMapper() {
    }

    public static ProjectDTO toDto(Project project) {
        if (project == null) {
            return null;
        }
        return ProjectDTO.builder()
                .id(project.getId())
                .name(project.getName())
                .description(project.getDescription())
                .isActive(project.isActive())
                .build();
    }

    // createdBy is passed in rather than looked up here - mappers don't
    // touch repositories, that lookup (resolving the authenticated
    // manager's User row) is the service's job.
    public static Project toEntity(CreateProjectRequest request, User createdBy) {
        if (request == null) {
            return null;
        }
        return Project.builder()
                .name(request.getName())
                .description(request.getDescription())
                .isActive(true)
                .createdBy(createdBy)
                .build();
    }

    // In-place edit: name/description only. isActive is deliberately not
    // touched here - it's updated only via the dedicated deactivate flow
    // (ProjectService.deactivateProject), never folded into this
    // general-purpose edit, per the design decision already noted on
    // UpdateProjectRequest.
    public static void applyToEntity(Project existing, UpdateProjectRequest request) {
        existing.setName(request.getName());
        existing.setDescription(request.getDescription());
    }
}
