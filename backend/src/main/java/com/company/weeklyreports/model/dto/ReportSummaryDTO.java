package com.company.weeklyreports.model.dto;

import com.company.weeklyreports.model.entity.ReportStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Lightweight view of a Report for list endpoints (history table, dashboard,
 * review queue). Deliberately has no nested task/blocker/achievement/hours
 * collections - those are only needed one report at a time, and including
 * them here would mean fetching and serializing four child lists per row
 * on every page of a paginated list.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReportSummaryDTO {

    private Long id;

    private Long userId;

    private String userName;

    private String projectName;

    private LocalDate weekStartDate;

    private LocalDate weekEndDate;

    private ReportStatus status;

    private LocalDateTime updatedAt;
}
