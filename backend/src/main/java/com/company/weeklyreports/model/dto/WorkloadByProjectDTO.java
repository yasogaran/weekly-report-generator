package com.company.weeklyreports.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Task count per project, for the manager's workload-distribution chart.
 * Field order matches TaskEntryRepository.findWorkloadByProject()'s JPQL
 * constructor expression - built directly by Hibernate, not application code.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WorkloadByProjectDTO {

    private Long projectId;

    private String projectName;

    private long taskCount;
}
