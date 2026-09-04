package com.company.weeklyreports.model.dto;

import com.company.weeklyreports.model.entity.TaskEntryType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Same shape used for both request (nested in CreateReportRequest) and response (nested in
 * ReportDTO) — api-doc.md shows no difference between the two directions for task entries.
 * Needs a no-arg constructor + setters (not just @Builder) so Jackson can deserialize it as
 * a nested field inside an incoming request body.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TaskEntryDTO {

    @NotBlank(message = "is required")
    private String taskName;

    private String priority;

    @NotNull(message = "is required")
    private Integer plannedPercent;

    @NotNull(message = "is required")
    private Integer actualPercent;

    private String status;

    private Double timePlanned;

    private Double timeSpent;

    private String deliverable;

    @NotNull(message = "is required")
    private TaskEntryType entryType;
}
