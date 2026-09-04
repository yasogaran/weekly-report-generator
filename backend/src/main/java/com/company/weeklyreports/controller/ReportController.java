package com.company.weeklyreports.controller;

import com.company.weeklyreports.exception.ErrorResponse;
import com.company.weeklyreports.model.dto.CreateReportRequest;
import com.company.weeklyreports.model.dto.ReportDTO;
import com.company.weeklyreports.model.dto.ReportFilterParams;
import com.company.weeklyreports.model.dto.ReportSummaryDTO;
import com.company.weeklyreports.model.dto.UpdateReportRequest;
import com.company.weeklyreports.model.entity.ReportStatus;
import com.company.weeklyreports.model.entity.User;
import com.company.weeklyreports.security.CustomUserPrincipal;
import com.company.weeklyreports.service.ReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

/**
 * HTTP entry points for the Report lifecycle: create, edit, submit, and
 * read (single + paginated list + version history). Thin by design -
 * every method just resolves the caller and delegates straight to
 * ReportService, which owns all business rules and RBAC scoping.
 *
 * Exception handling note: none of these methods catch anything yet
 * (GlobalExceptionHandler is a separate task). Once it exists, expect
 * ResourceNotFoundException -> 404, ValidationException -> 400, and
 * InvalidStateTransitionException -> 409 to be mapped there - see the
 * explanation delivered alongside this task for why 409 fits the state
 * transition case specifically.
 */
@RestController
@RequestMapping("/api/reports")
@Tag(name = "Reports", description = "Report lifecycle: create, edit, submit, and read")
public class ReportController {

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    // POST /api/reports - creates a new DRAFT report owned by the caller.
    // rbac-matrix.md denies report creation to managers outright (this is
    // a role-only rule, not an ownership one - a manager has no report to
    // "own" yet at creation time, so there's nothing for the service layer
    // to scope), so this is the one report endpoint that DOES need
    // @PreAuthorize rather than relying on ReportService.
    @Operation(summary = "Create a new DRAFT report owned by the caller")
    @ApiResponse(responseCode = "400", description = "Validation failed",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "403", description = "Caller is not a TEAM_MEMBER",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    @PostMapping
    @PreAuthorize("hasRole('TEAM_MEMBER')")
    public ReportDTO createDraft(@Valid @RequestBody CreateReportRequest request, Authentication authentication) {
        return reportService.createDraft(request, currentUser(authentication));
    }

    // PATCH /api/reports/{id} - edits a report the caller owns. No
    // @PreAuthorize here: any authenticated user (either role) may call
    // this endpoint, but ReportService.updateReport only ever fetches via
    // findByIdAndUserId, so it only ever finds/edits reports the caller
    // themselves own. Since managers cannot create reports (see above),
    // no manager ever owns one to edit - the same "denied" outcome
    // rbac-matrix.md requires, just enforced by ownership scoping rather
    // than a role annotation.
    @Operation(summary = "Edit a caller-owned report; forks a new version if it was NEEDS_CORRECTION")
    @ApiResponse(responseCode = "400", description = "Validation failed",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "404", description = "Report not found, or not owned by the caller",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "409", description = "Report is SUBMITTED or APPROVED and cannot be edited",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    @PatchMapping("/{id}")
    public ReportDTO updateReport(@PathVariable Long id,
                                   @Valid @RequestBody UpdateReportRequest request,
                                   Authentication authentication) {
        return reportService.updateReport(id, request, currentUser(authentication));
    }

    // POST /api/reports/{id}/submit - transitions a caller-owned report to
    // SUBMITTED. Same reasoning as updateReport: no @PreAuthorize needed,
    // ownership scoping in ReportService.submitReport (findByIdAndUserId)
    // already means only the owning team member can ever reach a report
    // to submit it.
    @Operation(summary = "Submit a caller-owned report, moving it to SUBMITTED")
    @ApiResponse(responseCode = "404", description = "Report not found, or not owned by the caller",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "409", description = "Report's current status cannot transition to SUBMITTED",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    @PostMapping("/{id}/submit")
    public ReportDTO submitReport(@PathVariable Long id, Authentication authentication) {
        return reportService.submitReport(id, currentUser(authentication));
    }

    // GET /api/reports/{id} - fetches one report. No @PreAuthorize: both
    // roles are allowed to call this endpoint, and ReportService.
    // getReportById itself branches by role (manager: any report; team
    // member: owned only, via ReportAccessGuard) - the access decision
    // depends on data (whose report is it) that only the service layer
    // has, not something a static role annotation on the endpoint could
    // express.
    @Operation(summary = "Fetch one report by id, RBAC-scoped (managers: any report; team members: own only)")
    @ApiResponse(responseCode = "404", description = "Report not found, or not accessible to the caller",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    @GetMapping("/{id}")
    public ReportDTO getReportById(@PathVariable Long id, Authentication authentication) {
        return reportService.getReportById(id, currentUser(authentication));
    }

    // GET /api/reports - paginated, filterable report list. No
    // @PreAuthorize: any authenticated user may call this endpoint, but
    // ReportService.listReports forces memberId back to the caller's own
    // id for team members regardless of what filters they send, so the
    // RBAC scoping happens inside the service, not here. Uses Spring's
    // built-in Pageable resolver (page/size/sort query params) rather than
    // hand-rolled pagination parameters.
    @Operation(summary = "Paginated, filterable report list, RBAC-scoped by role")
    @GetMapping
    public Page<ReportSummaryDTO> listReports(@RequestParam(required = false) Long memberId,
                                               @RequestParam(required = false) Long projectId,
                                               @RequestParam(required = false) ReportStatus status,
                                               @RequestParam(required = false) LocalDate weekStart,
                                               @RequestParam(required = false) LocalDate weekEnd,
                                               Pageable pageable,
                                               Authentication authentication) {
        ReportFilterParams filters = ReportFilterParams.builder()
                .memberId(memberId)
                .projectId(projectId)
                .status(status)
                .weekStart(weekStart)
                .weekEnd(weekEnd)
                .build();
        return reportService.listReports(filters, pageable, currentUser(authentication));
    }

    // GET /api/reports/{id}/versions - the full correction-cycle version
    // chain for one report, newest first. No @PreAuthorize: same reasoning
    // as getReportById - ReportService.getVersionHistory enforces access
    // on the requested id via ReportAccessGuard before walking the chain.
    @Operation(summary = "Full correction-cycle version history for a report, newest first")
    @ApiResponse(responseCode = "404", description = "Report not found, or not accessible to the caller",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    @GetMapping("/{id}/versions")
    public List<ReportDTO> getVersionHistory(@PathVariable Long id, Authentication authentication) {
        return reportService.getVersionHistory(id, currentUser(authentication));
    }

    // JwtAuthFilter sets the Authentication principal to a
    // CustomUserPrincipal (built by CustomUserDetailsService from a fresh
    // database read on every request). That wrapper embeds the full User
    // entity, so this is a plain cast + accessor call - no second database
    // hit needed here just to find out who's calling.
    private User currentUser(Authentication authentication) {
        return ((CustomUserPrincipal) authentication.getPrincipal()).getUser();
    }
}
