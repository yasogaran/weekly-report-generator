package com.company.weeklyreports.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * {@code @JsonProperty("isKeyAchievement")} is required, not decorative — same reasoning as
 * BlockerDTO.isKeyIssue (see UserDTO's javadoc for the full Jackson/Lombok explanation).
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AchievementDTO {

    @NotBlank(message = "is required")
    private String description;

    @JsonProperty("isKeyAchievement")
    private boolean isKeyAchievement;
}
