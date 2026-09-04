package com.company.weeklyreports.model.dto;

import com.company.weeklyreports.model.entity.ReportStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * One (member, status) bucket for the manager's status-breakdown chart -
 * e.g. "how many reports does each member currently have in each status."
 * Field order matches ReportRepository.findStatusByMember()'s JPQL
 * constructor expression - built directly by Hibernate, not application code.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StatusByMemberDTO {

    private Long userId;

    private String userName;

    private ReportStatus status;

    private long count;
}
