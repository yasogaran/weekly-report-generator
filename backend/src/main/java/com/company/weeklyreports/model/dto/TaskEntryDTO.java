package com.company.weeklyreports.model.dto;

import com.company.weeklyreports.model.entity.EntryType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * Read-only view of one task line item, nested inside ReportDTO.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TaskEntryDTO {

    private Long id;

    private String taskName;

    private String priority;

    private Integer plannedPercent;

    private Integer actualPercent;

    private String status;

    private BigDecimal timePlanned;

    private BigDecimal timeSpent;

    private String deliverable;

    private EntryType entryType;
}
