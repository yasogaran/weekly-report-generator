package com.company.weeklyreports.model.dto;

import com.company.weeklyreports.model.entity.ReportStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

/**
 * Optional filters for GET /reports. All fields are nullable - an absent
 * field means "don't filter on this." memberId/projectId/status only have
 * any effect for a manager; a team member's results are always scoped to
 * their own reports regardless of what's set here (enforced in
 * ReportServiceImpl, not here - this is a plain data holder).
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class ReportFilterParams {

    private Long memberId;

    private Long projectId;

    private ReportStatus status;

    // Inclusive week-range bounds, matched against weekStartDate/weekEndDate.
    private LocalDate weekStart;

    private LocalDate weekEnd;
}
