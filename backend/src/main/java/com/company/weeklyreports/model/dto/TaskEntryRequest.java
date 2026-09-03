package com.company.weeklyreports.model.dto;

import com.company.weeklyreports.model.entity.EntryType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * One task line item as submitted inside CreateReportRequest/UpdateReportRequest.
 * No id field - the whole task list is replaced wholesale on each save
 * rather than diffed/patched entry by entry (kept simple per system-design.md).
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TaskEntryRequest {

    @NotBlank
    private String taskName;

    private String priority;

    private Integer plannedPercent;

    private Integer actualPercent;

    private String status;

    private BigDecimal timePlanned;

    private BigDecimal timeSpent;

    private String deliverable;

    @NotNull
    private EntryType entryType;
}
