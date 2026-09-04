package com.company.weeklyreports.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

/**
 * One point on the "completed tasks over time" chart. Field order matches
 * ReportRepository.findTaskTrend()'s JPQL constructor expression exactly -
 * that query builds these directly, so this DTO's constructor is called by
 * Hibernate, not by application code.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TaskTrendPointDTO {

    private LocalDate weekStartDate;

    private long completedTaskCount;
}
