package com.company.weeklyreports.model.dto;

import com.company.weeklyreports.model.entity.ReviewActionType;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Payload for POST /reports/{id}/review (manager only). comment is not
 * marked @NotBlank here because it's only required when action is
 * REQUESTED_CHANGES - that's a cross-field rule, so it belongs in the
 * service layer's validation, not a static annotation on this DTO.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReviewRequest {

    @NotNull
    private ReviewActionType action;

    private String comment;
}
