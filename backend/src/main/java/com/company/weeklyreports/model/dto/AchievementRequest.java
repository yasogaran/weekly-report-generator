package com.company.weeklyreports.model.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * One achievement as submitted inside CreateReportRequest/UpdateReportRequest.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AchievementRequest {

    @NotBlank
    private String description;

    private boolean isKeyAchievement;
}
