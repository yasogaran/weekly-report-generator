package com.company.weeklyreports.model.dto;

import com.company.weeklyreports.model.entity.ReviewActionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Read-only view of one review decision, used on the review page's history
 * list (GET /reports/{id}/reviews). Only exposes the reviewer's name, not
 * their full UserDTO - that's all this view needs.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReviewActionDTO {

    private Long id;

    private String reviewerName;

    private ReviewActionType action;

    private String comment;

    private LocalDateTime createdAt;
}
