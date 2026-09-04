package com.company.weeklyreports.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * Response shape for every Projects endpoint (docs/api/api-doc.md).
 * {@code @JsonProperty("isActive")} + {@code @Setter} are both required, not decorative —
 * see UserDTO's javadoc for the full explanation (without a setter, Jackson emits both
 * "active" AND "isActive" instead of merging them into the one annotated name).
 */
@Getter
@Setter
@AllArgsConstructor
@Builder
public class ProjectDTO {
    private Long id;
    private String name;
    private String description;

    @JsonProperty("isActive")
    private boolean isActive;
}
