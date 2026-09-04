package com.company.weeklyreports.model.dto;

import com.company.weeklyreports.model.entity.ReportStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

/** GET /dashboard/charts/status-by-member row (api-doc.md). */
@Getter
@AllArgsConstructor
@Builder
public class StatusByMemberDTO {
    private Long userId;
    private String userName;
    private ReportStatus status;
    private long count;
}
