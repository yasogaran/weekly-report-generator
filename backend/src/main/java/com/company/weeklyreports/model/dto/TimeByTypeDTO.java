package com.company.weeklyreports.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

/** GET /dashboard/charts/time-by-type row (api-doc.md), team-wide. */
@Getter
@AllArgsConstructor
@Builder
public class TimeByTypeDTO {
    private String taskType;
    private double totalHours;
}
