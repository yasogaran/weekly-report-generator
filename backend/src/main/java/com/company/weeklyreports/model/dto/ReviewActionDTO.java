package com.company.weeklyreports.model.dto;

import com.company.weeklyreports.model.entity.ReviewActionType;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

/** GET /reports/{id}/reviews row shape (api-doc.md). */
@Getter
@AllArgsConstructor
@Builder
public class ReviewActionDTO {
    private Long id;
    private String reviewerName;
    private ReviewActionType action;
    private String comment;
    private LocalDateTime createdAt;
}
