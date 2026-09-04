package com.company.weeklyreports.controller;

import com.company.weeklyreports.model.dto.ActivityFeedItemDTO;
import com.company.weeklyreports.model.dto.DashboardSummaryDTO;
import com.company.weeklyreports.model.dto.StatusByMemberDTO;
import com.company.weeklyreports.model.dto.TaskTrendPointDTO;
import com.company.weeklyreports.model.dto.TimeByTypeDTO;
import com.company.weeklyreports.model.dto.WorkloadByProjectDTO;
import com.company.weeklyreports.service.DashboardService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Every endpoint here is MANAGER only, no exceptions (api-doc.md) — one class-level @PreAuthorize covers all of them. */
@RestController
@RequestMapping("/dashboard")
@RequiredArgsConstructor
@PreAuthorize("hasRole('MANAGER')")
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/summary")
    public DashboardSummaryDTO getSummary() {
        return dashboardService.getSummary();
    }

    @GetMapping("/charts/task-trend")
    public List<TaskTrendPointDTO> getTaskTrend() {
        return dashboardService.getTaskTrend();
    }

    @GetMapping("/charts/status-by-member")
    public List<StatusByMemberDTO> getStatusByMember() {
        return dashboardService.getStatusByMember();
    }

    @GetMapping("/charts/workload-by-project")
    public List<WorkloadByProjectDTO> getWorkloadByProject() {
        return dashboardService.getWorkloadByProject();
    }

    @GetMapping("/charts/time-by-type")
    public List<TimeByTypeDTO> getTimeByType() {
        return dashboardService.getTimeByType();
    }

    @GetMapping("/activity-feed")
    public List<ActivityFeedItemDTO> getActivityFeed() {
        return dashboardService.getActivityFeed();
    }
}
