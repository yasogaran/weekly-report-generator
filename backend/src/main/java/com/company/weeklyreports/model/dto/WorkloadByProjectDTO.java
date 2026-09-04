package com.company.weeklyreports.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

/** GET /dashboard/charts/workload-by-project row (api-doc.md). */
@Getter
@AllArgsConstructor
@Builder
public class WorkloadByProjectDTO {
    private Long projectId;
    private String projectName;
    private long taskCount;
}
