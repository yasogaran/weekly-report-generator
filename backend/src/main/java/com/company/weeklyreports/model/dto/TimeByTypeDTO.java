package com.company.weeklyreports.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * Total hours logged per task type, team-wide (not scoped to any one
 * member). Field order matches HoursByTypeRepository.findTimeByType()'s
 * JPQL constructor expression - built directly by Hibernate, not
 * application code.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TimeByTypeDTO {

    private String taskType;

    private BigDecimal totalHours;
}
