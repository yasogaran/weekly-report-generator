package com.company.weeklyreports.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * Read-only view of one hours-by-type breakdown row, nested inside ReportDTO.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HoursByTypeDTO {

    private Long id;

    private String taskType;

    private BigDecimal hours;
}
