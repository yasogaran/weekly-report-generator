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
import com.company.weeklyreports.model.entity.User;
import com.company.weeklyreports.repository.ReportRepository;
import com.company.weeklyreports.repository.ReviewActionRepository;
import com.company.weeklyreports.repository.UserRepository;
import com.company.weeklyreports.security.ReportAccessGuard;
import com.company.weeklyreports.security.UserPrincipal;
import com.company.weeklyreports.service.ReportStatusMachine;
import com.company.weeklyreports.service.ReviewService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {

    private final ReportRepository reportRepository;
    private final UserRepository userRepository;
    private final ReviewActionRepository reviewActionRepository;
    private final ReportAccessGuard accessGuard;
    private final ReportStatusMachine statusMachine;
    private final ReportMapper reportMapper;
    private final ReviewMapper reviewMapper;

    @Override
    @Transactional
    public ReportDTO review(Long reportId, UserPrincipal reviewer, ReviewRequest request) {
        // No ownership dimension here (api-doc.md — managers aren't scoped to reports they
        // "own"), so this is a plain findById, not ReportAccessGuard.
        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new ResourceNotFoundException("Report not found: " + reportId));
        statusMachine.validateReviewable(report.getStatus());

        if (request.getAction() == ReviewActionType.REQUESTED_CHANGES && !StringUtils.hasText(request.getComment())) {
            throw new ValidationException("A comment is required when requesting changes.");
        }

        User reviewerUser = userRepository.findById(reviewer.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + reviewer.getId()));

        ReviewAction action = ReviewAction.builder()
                .report(report)
                .reviewer(reviewerUser)
                .action(request.getAction())
                .comment(request.getComment())
                .build();
        reviewActionRepository.save(action);

        report.setStatus(
                request.getAction() == ReviewActionType.APPROVED
                        ? ReportStatus.APPROVED
                        : ReportStatus.NEEDS_CORRECTION);
        return reportMapper.toDto(reportRepository.save(report));
    }

    @Override
    public List<ReviewActionDTO> getReviews(Long reportId, UserPrincipal requester) {
        // Ownership-checked here (unlike review() above) — a team member may only see their
        // own report's review history; a manager can see any (api-doc.md).
        Report report = accessGuard.requireAccessible(reportId, requester);
        return reviewActionRepository.findByReportIdOrderByCreatedAtDesc(report.getId()).stream()
                .map(reviewMapper::toDto)
                .toList();
    }
}
