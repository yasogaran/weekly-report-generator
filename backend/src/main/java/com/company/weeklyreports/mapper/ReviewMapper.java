package com.company.weeklyreports.mapper;

import com.company.weeklyreports.model.dto.ReviewActionDTO;
import com.company.weeklyreports.model.entity.ReviewAction;

/**
 * Converts a ReviewAction entity to its DTO. Written by hand (no MapStruct)
 * so every field mapping is explicit and easy to explain.
 */
public class ReviewMapper {

    private ReviewMapper() {
    }

    // Flattens reviewer down to just their name - the review history list
    // only needs to display "who reviewed this", not a full UserDTO.
    public static ReviewActionDTO toDto(ReviewAction reviewAction) {
        if (reviewAction == null) {
            return null;
        }
        return ReviewActionDTO.builder()
                .id(reviewAction.getId())
                .reviewerName(reviewAction.getReviewer().getName())
                .action(reviewAction.getAction())
                .comment(reviewAction.getComment())
                .createdAt(reviewAction.getCreatedAt())
                .build();
    }
}
