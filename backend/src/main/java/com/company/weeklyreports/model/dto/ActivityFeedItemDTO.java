package com.company.weeklyreports.model.dto;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

/** GET /dashboard/activity-feed row (api-doc.md). `type` matches frontend/lib/dashboardTypes.ts's ActivityType exactly. */
@Getter
@AllArgsConstructor
@Builder
public class ActivityFeedItemDTO {
    private String type;
    private Long reportId;
    private String userName;
    private LocalDateTime timestamp;
    private String detail;
}
