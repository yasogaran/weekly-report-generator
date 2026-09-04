package com.company.weeklyreports.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

/** GET /dashboard/summary (api-doc.md). Field names must match frontend/lib/dashboardTypes.ts exactly. */
@Getter
@AllArgsConstructor
@Builder
public class DashboardSummaryDTO {
    private long totalSubmittedThisWeek;
    private double complianceRate;
    private long needsCorrectionCount;
    private long openBlockersCount;
}
