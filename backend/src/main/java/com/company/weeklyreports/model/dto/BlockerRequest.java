package com.company.weeklyreports.model.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * One blocker as submitted inside CreateReportRequest/UpdateReportRequest.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BlockerRequest {

    @NotBlank
    private String description;

    private boolean isKeyIssue;
}
