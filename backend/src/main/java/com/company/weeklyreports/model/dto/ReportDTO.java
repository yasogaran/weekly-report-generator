package com.company.weeklyreports.model.dto;

import com.company.weeklyreports.model.entity.ReportStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Full detail view of a Report, used by the single-report GET endpoint
 * (create/edit form prefill, review page, read-only view). Carries all four
 * child collections - heavier than ReportSummaryDTO on purpose, since this
 * is only ever fetched one report at a time, not in a list.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReportDTO {

    private Long id;

    // Denormalized IDs + display names so the frontend never has to make a
    // second call just to show "who" and "which project" on the report.
    private Long userId;

    private String userName;

    private Long projectId;

    private String projectName;

    private LocalDate weekStartDate;

    private LocalDate weekEndDate;

    private ReportStatus status;

    private int versionNumber;

    private Long parentReportId;

    private String notes;

    private List<TaskEntryDTO> taskEntries;

    private List<BlockerDTO> blockers;

    private List<AchievementDTO> achievements;

    private List<HoursByTypeDTO> hoursByType;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private LocalDateTime submittedAt;
}
