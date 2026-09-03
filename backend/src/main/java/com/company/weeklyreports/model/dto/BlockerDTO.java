package com.company.weeklyreports.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Read-only view of one blocker, nested inside ReportDTO.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BlockerDTO {

    private Long id;

    private String description;

    private boolean isKeyIssue;
}
