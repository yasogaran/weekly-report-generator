package com.company.weeklyreports.controller;

import com.company.weeklyreports.exception.ErrorResponse;
import com.company.weeklyreports.model.dto.ActivityFeedItemDTO;
import com.company.weeklyreports.model.dto.DashboardSummaryDTO;
import com.company.weeklyreports.model.dto.StatusByMemberDTO;
import com.company.weeklyreports.model.dto.TaskTrendPointDTO;
import com.company.weeklyreports.model.dto.TimeByTypeDTO;
import com.company.weeklyreports.model.dto.WorkloadByProjectDTO;
import com.company.weeklyreports.service.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * HTTP entry points for the manager dashboard. Class-level @PreAuthorize:
 * per rbac-matrix.md's "Team Dashboard & Visual Insights" row, dashboard
 * access is flatly manager-only with no ownership dimension at all - every
 * endpoint here needs the identical gate, so it's declared once for the
 * whole controller rather than repeated per method.
 *
 * Paths follow system-design.md section 5's /api/dashboard/summary and
 * /api/dashboard/charts/* patterns for the five chart/summary endpoints.
 * activity-feed isn't listed in system-design.md explicitly (the spec only
 * shows summary and charts/* as examples) - it doesn't fit either of those
 * two shapes cleanly (it's neither a chart nor a summary number), so it
 * gets its own top-level path; flagging this as my own naming choice.
 */
@RestController
@RequestMapping("/api/dashboard")
@PreAuthorize("hasRole('MANAGER')")
@Tag(name = "Dashboard", description = "Manager-only aggregate metrics and charts")
public class DashboardController {

    private static final int DEFAULT_ACTIVITY_FEED_LIMIT = 20;

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @Operation(summary = "Top-line summary metrics: submissions this week, compliance rate, needs-correction and open-blocker counts")
    @ApiResponse(responseCode = "403", description = "Caller is not a MANAGER",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    @GetMapping("/summary")
    public DashboardSummaryDTO getSummary() {
        return dashboardService.getSummary();
    }

    @Operation(summary = "Completed-task count grouped by the week each report was about")
    @ApiResponse(responseCode = "403", description = "Caller is not a MANAGER",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    @GetMapping("/charts/task-trend")
    public List<TaskTrendPointDTO> getTaskTrend() {
        return dashboardService.getTaskTrend();
    }

    @Operation(summary = "Report count grouped by member and status")
    @ApiResponse(responseCode = "403", description = "Caller is not a MANAGER",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    @GetMapping("/charts/status-by-member")
    public List<StatusByMemberDTO> getStatusByMember() {
        return dashboardService.getStatusByMember();
    }

    @Operation(summary = "Task count grouped by project")
    @ApiResponse(responseCode = "403", description = "Caller is not a MANAGER",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    @GetMapping("/charts/workload-by-project")
    public List<WorkloadByProjectDTO> getWorkloadByProject() {
        return dashboardService.getWorkloadByProject();
    }

    @Operation(summary = "Total hours logged grouped by task type, team-wide")
    @ApiResponse(responseCode = "403", description = "Caller is not a MANAGER",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    @GetMapping("/charts/time-by-type")
    public List<TimeByTypeDTO> getTimeByType() {
        return dashboardService.getTimeByType();
    }

    @Operation(summary = "Most recent submission and review activity, newest first")
    @ApiResponse(responseCode = "403", description = "Caller is not a MANAGER",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    @GetMapping("/activity-feed")
    public List<ActivityFeedItemDTO> getActivityFeed(
            @RequestParam(defaultValue = "" + DEFAULT_ACTIVITY_FEED_LIMIT) int limit) {
        return dashboardService.getActivityFeed(limit);
    }
}
