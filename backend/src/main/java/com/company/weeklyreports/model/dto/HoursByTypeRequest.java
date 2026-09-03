package com.company.weeklyreports.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * One hours-by-type row as submitted inside CreateReportRequest/UpdateReportRequest.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HoursByTypeRequest {

    @NotBlank
    private String taskType;

    @NotNull
    @Positive
    private BigDecimal hours;
}
