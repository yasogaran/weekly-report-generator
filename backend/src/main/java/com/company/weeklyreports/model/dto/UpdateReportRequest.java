package com.company.weeklyreports.model.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

/**
 * Payload for PATCH /reports/{id} - editing a report while it's still in
 * DRAFT or NEEDS_CORRECTION (enforced by the service, not here). Same
 * shape as CreateReportRequest and, like it, carries no status field.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateReportRequest {

    @NotNull
    private Long projectId;

    @NotNull
    private LocalDate weekStartDate;

    @NotNull
    private LocalDate weekEndDate;

    private String notes;

    private List<TaskEntryRequest> taskEntries;

    private List<BlockerRequest> blockers;

    private List<AchievementRequest> achievements;

    private List<HoursByTypeRequest> hoursByType;
}
