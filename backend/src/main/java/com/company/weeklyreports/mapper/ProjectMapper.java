package com.company.weeklyreports.mapper;

import com.company.weeklyreports.model.dto.ProjectDTO;
import com.company.weeklyreports.model.entity.Project;
import org.springframework.stereotype.Component;

@Component
public class ProjectMapper {

    public ProjectDTO toDto(Project project) {
        return ProjectDTO.builder()
                .id(project.getId())
                .name(project.getName())
                .description(project.getDescription())
                .isActive(project.isActive())
                .build();
    }
}
