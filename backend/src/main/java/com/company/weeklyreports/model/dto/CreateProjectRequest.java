package com.company.weeklyreports.model.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Payload for POST /projects (manager only). isActive/createdBy are not
 * settable here - a new project always starts active, and createdBy comes
 * from the authenticated manager, not the request body.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateProjectRequest {

    @NotBlank
    private String name;

    private String description;
}
