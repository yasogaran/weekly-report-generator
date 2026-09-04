package com.company.weeklyreports.model.dto;

import com.company.weeklyreports.model.entity.ReportStatus;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

/** Full report shape — GET /reports/{id} and every mutating report endpoint's response (api-doc.md). */
@Getter
@AllArgsConstructor
@Builder
public class ReportDTO {
    private Long id;
    private Long userId;
    private String userName;
    private Long projectId;
    private String projectName;
    private LocalDate weekStartDate;
    private LocalDate weekEndDate;
    private ReportStatus status;
    private Integer versionNumber;
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
