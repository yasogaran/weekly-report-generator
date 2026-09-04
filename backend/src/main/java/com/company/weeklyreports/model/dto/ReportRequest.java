package com.company.weeklyreports.model.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

/**
 * Shared shape for POST /reports (CreateReportRequest) and PATCH /reports/{id}
 * (UpdateReportRequest) — api-doc.md: "same shape as CreateReportRequest". One class instead
 * of two near-identical ones; the difference between create/update is entirely in what
 * ReportServiceImpl does with it, not in the shape itself.
 */
@Getter
@Setter
public class ReportRequest {

    @NotNull(message = "is required")
    private Long projectId;

    @NotNull(message = "is required")
    private LocalDate weekStartDate;

    @NotNull(message = "is required")
    private LocalDate weekEndDate;

    private String notes;

    @Valid
    private List<TaskEntryDTO> taskEntries = new ArrayList<>();

    @Valid
    private List<BlockerDTO> blockers = new ArrayList<>();

    @Valid
    private List<AchievementDTO> achievements = new ArrayList<>();

    @Valid
    private List<HoursByTypeDTO> hoursByType = new ArrayList<>();
}
