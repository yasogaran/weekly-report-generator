package com.company.weeklyreports.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * One entry in the manager dashboard's recent-activity feed. type is a
 * plain String rather than an enum because it spans two different source
 * entities with no shared type: "SUBMITTED" comes from Report.submittedAt
 * (ReportRepository.findRecentSubmissions()), while "APPROVED"/
 * "REQUESTED_CHANGES" come from ReviewAction.action
 * (ReviewActionRepository.findRecentReviewActivity()) - see
 * DashboardService.getActivityFeed() for how the two sources are merged.
 * Field order matches both queries' JPQL constructor expressions.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ActivityFeedItemDTO {

    private String type;

    private Long reportId;

    private String userName;

    private LocalDateTime timestamp;

    private String detail;
}
