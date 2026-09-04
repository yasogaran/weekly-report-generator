package com.company.weeklyreports.mapper;

import com.company.weeklyreports.model.dto.ReviewActionDTO;
import com.company.weeklyreports.model.entity.ReviewAction;
import org.springframework.stereotype.Component;

@Component
public class ReviewMapper {

    public ReviewActionDTO toDto(ReviewAction action) {
        return ReviewActionDTO.builder()
                .id(action.getId())
                .reviewerName(action.getReviewer().getName())
                .action(action.getAction())
                .comment(action.getComment())
                .createdAt(action.getCreatedAt())
                .build();
    }
}
