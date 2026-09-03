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
 * Payload for POST /reports. No status field on purpose - a report is
 * always created as DRAFT by the service; status only ever changes via the
 * dedicated submit/review endpoints, never through a generic report edit.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateReportRequest {

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
