package com.company.weeklyreports.mapper;

import com.company.weeklyreports.model.dto.CreateProjectRequest;
import com.company.weeklyreports.model.dto.ProjectDTO;
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
}
