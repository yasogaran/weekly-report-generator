package com.company.weeklyreports.model.dto;

import com.company.weeklyreports.model.entity.ReviewActionType;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

/**
 * POST /reports/{id}/reviews body (api-doc.md). Whether `comment` is required depends on
 * `action` (required + non-blank for REQUESTED_CHANGES, optional for APPROVED) — that's a
 * cross-field rule bean validation can't express cleanly, so it's checked in
 * ReviewServiceImpl instead of here (see ValidationException usages there).
 */
@Getter
@Setter
public class ReviewRequest {

    @NotNull(message = "is required")
    private ReviewActionType action;

    private String comment;
}
