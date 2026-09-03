package com.company.weeklyreports.model.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Payload for PATCH /projects/{id} (manager only). isActive is updated via
 * the dedicated activate/deactivate flow the service exposes, not folded
 * into this general-purpose edit request.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateProjectRequest {

    @NotBlank
    private String name;

    private String description;
}
