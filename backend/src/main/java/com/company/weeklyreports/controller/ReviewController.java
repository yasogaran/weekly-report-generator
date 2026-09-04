package com.company.weeklyreports.controller;

import com.company.weeklyreports.exception.ErrorResponse;
import com.company.weeklyreports.model.dto.ReportDTO;
import com.company.weeklyreports.model.dto.ReviewActionDTO;
import com.company.weeklyreports.model.dto.ReviewRequest;
import com.company.weeklyreports.model.entity.User;
import com.company.weeklyreports.security.CustomUserPrincipal;
import com.company.weeklyreports.service.ReviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * HTTP entry points for the manager review flow: submitting a review
 * decision on a report, and reading a report's review history. Thin by
 * design - delegates straight to ReviewService.
 *
 * Exception handling note: same as ReportController - no handling here yet
 * (GlobalExceptionHandler is a separate task). Expect ResourceNotFoundException
 * -> 404, ValidationException -> 400 (e.g. the missing-comment-on-
 * REQUEST_CHANGES rule), InvalidStateTransitionException -> 409 to be
 * mapped there eventually.
 */
@RestController
@RequestMapping("/api/reports/{id}/reviews")
@Tag(name = "Reviews", description = "Manager review decisions on submitted reports")
public class ReviewController {

    private final ReviewService reviewService;

    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    // POST /api/reports/{id}/reviews - applies a manager's review decision
    // to a report. This DOES need @PreAuthorize: unlike every report
    // endpoint above, review authority is a pure role check with no
    // ownership/ownership-like dimension at all (a manager reviewing
    // report #47 doesn't "own" #47 in any sense the service layer could
    // scope by) - rbac-matrix.md denies this action to team members
    // outright, regardless of which report is targeted. So the role gate
    // belongs here, at the endpoint, rather than being inferred from data
    // the service would otherwise have to fetch just to reject the call.
    @Operation(summary = "Apply a manager's review decision (approve or request changes) to a report")
    @ApiResponse(responseCode = "400", description = "Comment is required when requesting changes",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "403", description = "Caller is not a MANAGER",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "404", description = "Report not found",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "409", description = "Report's current status cannot transition via this action",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    @PostMapping
    @PreAuthorize("hasRole('MANAGER')")
    public ReportDTO submitReview(@PathVariable Long id,
                                   @Valid @RequestBody ReviewRequest request,
                                   Authentication authentication) {
        return reviewService.submitReview(id, request, currentUser(authentication));
    }

    // GET /api/reports/{id}/reviews - a report's review history. No
    // @PreAuthorize: any authenticated user may call this endpoint, but
    // ReviewService.getReviewHistory enforces access via ReportAccessGuard
    // (team member: owned report only; manager: any report) before
    // returning anything - the same data-dependent access decision
    // pattern as ReportController's read endpoints.
    @Operation(summary = "Full review history for a report, newest first, RBAC-scoped")
    @ApiResponse(responseCode = "404", description = "Report not found, or not accessible to the caller",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    @GetMapping
    public List<ReviewActionDTO> getReviewHistory(@PathVariable Long id, Authentication authentication) {
        return reviewService.getReviewHistory(id, currentUser(authentication));
    }

    // Same mechanism as ReportController.currentUser() - see there for the
    // full explanation. Duplicated rather than shared since there's no
    // common controller base class in this codebase.
    private User currentUser(Authentication authentication) {
        return ((CustomUserPrincipal) authentication.getPrincipal()).getUser();
    }
}
