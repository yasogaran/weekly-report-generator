package com.company.weeklyreports.model.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

/** Shared shape for POST /projects and PATCH /projects/{id} — identical request body (api-doc.md). */
@Getter
@Setter
public class ProjectRequest {

    @NotBlank(message = "is required")
    private String name;

    private String description;
}
