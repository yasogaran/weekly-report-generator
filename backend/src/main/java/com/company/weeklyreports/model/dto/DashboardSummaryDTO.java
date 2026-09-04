package com.company.weeklyreports.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Top-line manager dashboard metrics for GET /api/dashboard/summary. Built
 * by DashboardService from several independent aggregate queries (not one
 * JPQL projection), since the four numbers here come from three different
 * entities (Report, Blocker, User) with no single natural join between all
 * of them - see DashboardService.getSummary() for how each field is
 * computed, and the explicit compliance-rate definition there.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DashboardSummaryDTO {

    private long totalSubmittedThisWeek;

    private double complianceRate;

    private long needsCorrectionCount;

    private long openBlockersCount;
}
