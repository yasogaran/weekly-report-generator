package com.company.weeklyreports.model.dto;

import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

/** GET /dashboard/charts/task-trend row (api-doc.md). */
@Getter
@AllArgsConstructor
@Builder
public class TaskTrendPointDTO {
    private LocalDate weekStartDate;
    private long completedTaskCount;
}
