package com.company.weeklyreports.model.dto;

import com.company.weeklyreports.model.entity.ReportStatus;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

/** Lighter row shape for GET /reports (list) — no nested collections (api-doc.md). */
@Getter
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
