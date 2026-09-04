package com.company.weeklyreports.service.impl;

import com.company.weeklyreports.exception.ResourceNotFoundException;
import com.company.weeklyreports.exception.ValidationException;
import com.company.weeklyreports.mapper.ReportMapper;
import com.company.weeklyreports.mapper.ReviewMapper;
import com.company.weeklyreports.model.dto.ReportDTO;
import com.company.weeklyreports.model.dto.ReviewActionDTO;
import com.company.weeklyreports.model.dto.ReviewRequest;
import com.company.weeklyreports.model.entity.Report;
import com.company.weeklyreports.model.entity.ReportStatus;
import com.company.weeklyreports.model.entity.ReviewAction;
import com.company.weeklyreports.model.entity.ReviewActionType;
import com.company.weeklyreports.model.entity.Role;
import com.company.weeklyreports.model.entity.User;
import com.company.weeklyreports.repository.ReportRepository;
import com.company.weeklyreports.repository.ReviewActionRepository;
import com.company.weeklyreports.service.ReportAccessGuard;
import com.company.weeklyreports.service.ReportStateMachineService;
import com.company.weeklyreports.service.ReportTransitionAction;
import com.company.weeklyreports.service.ReviewService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Implements the manager review flow: validating and applying a review
 * decision (via the shared ReportStateMachineService, never an inline
 * if/else on status), recording an append-only ReviewAction, and reading
 * review history under the same ownership rule ReportService uses.
 */
@Service
public class ReviewServiceImpl implements ReviewService {

    private final ReportRepository reportRepository;
    private final ReviewActionRepository reviewActionRepository;
    private final ReportStateMachineService reportStateMachineService;
    private final ReportAccessGuard reportAccessGuard;

    public ReviewServiceImpl(ReportRepository reportRepository,
                              ReviewActionRepository reviewActionRepository,
                              ReportStateMachineService reportStateMachineService,
                              ReportAccessGuard reportAccessGuard) {
        this.reportRepository = reportRepository;
        this.reviewActionRepository = reviewActionRepository;
        this.reportStateMachineService = reportStateMachineService;
        this.reportAccessGuard = reportAccessGuard;
    }

    // Deliberately looks the report up by plain findById, not an
    // ownership-scoped fetch - see the standalone explanation on why a
    // manager reviewing a report is never "their own" report to scope by.
    //
    // The comment-required-if-REQUEST_CHANGES rule can't be a @NotBlank (or
    // any other) annotation on ReviewRequest.comment, because it isn't
    // unconditionally required - it's required only when action ==
    // REQUESTED_CHANGES, and optional when action == APPROVED. Bean
    // validation annotations validate one field in isolation; this is a
    // cross-field rule (comment's requiredness depends on action's value),
    // so it has to be checked here in the service, where both fields are
    // in scope at once.
    @Override
    @Transactional
    public ReportDTO submitReview(Long reportId, ReviewRequest request, User currentManager) {
        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new ResourceNotFoundException("Report not found: " + reportId));

        // isBlank() (not isEmpty()) so a whitespace-only comment - which a
        // careless "just hit space to get past validation" attempt would
        // produce - is rejected exactly like a genuinely empty string.
        if (request.getAction() == ReviewActionType.REQUESTED_CHANGES
                && (request.getComment() == null || request.getComment().isBlank())) {
            throw new ValidationException("A comment is required when requesting changes on a report");
        }

        ReportTransitionAction transitionAction = toTransitionAction(request.getAction());
        ReportStatus nextStatus = reportStateMachineService.transition(
                report.getStatus(), transitionAction, Role.MANAGER);

        report.setStatus(nextStatus);
        reportRepository.save(report);

        // Append-only: every review decision is its own new row, never an
        // update to a prior one, so the full comment/decision history
        // described in system-design.md stays intact across correction
        // cycles.
        ReviewAction reviewAction = ReviewAction.builder()
                .report(report)
                .reviewer(currentManager)
                .action(request.getAction())
                .comment(request.getComment())
                .build();
        reviewActionRepository.save(reviewAction);

        return ReportMapper.toDto(report);
    }

    // Team members may read the review comments on their OWN report only;
    // managers may read any. Reuses ReportAccessGuard (shared with
    // ReportServiceImpl) purely to enforce that rule - the fetched Report
    // itself isn't needed for anything else here.
    @Override
    @Transactional(readOnly = true)
    public List<ReviewActionDTO> getReviewHistory(Long reportId, User currentUser) {
        reportAccessGuard.fetchAccessible(reportId, currentUser);

        return reviewActionRepository.findByReport_IdOrderByCreatedAtDesc(reportId)
                .stream()
                .map(ReviewMapper::toDto)
                .collect(Collectors.toList());
    }

    private ReportTransitionAction toTransitionAction(ReviewActionType action) {
        return switch (action) {
            case APPROVED -> ReportTransitionAction.APPROVE;
            case REQUESTED_CHANGES -> ReportTransitionAction.REQUEST_CHANGES;
        };
    }
}
