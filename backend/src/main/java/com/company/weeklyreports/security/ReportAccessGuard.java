package com.company.weeklyreports.security;

import com.company.weeklyreports.exception.ResourceNotFoundException;
import com.company.weeklyreports.model.entity.Report;
import com.company.weeklyreports.repository.ReportRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Ownership enforcement for reports — lives here in a dedicated component (not
 * @PreAuthorize) because it's data-dependent ("whose report is it"), which a static role
 * annotation can't express (api-doc.md's own RBAC notes name this exact pattern:
 * "findByIdAndUserId, ReportAccessGuard"). A manager can access any report; a team member
 * only their own — and a report that exists but belongs to someone else 404s identically to
 * one that doesn't exist at all, deliberately, to prevent ID enumeration.
 */
@Component
@RequiredArgsConstructor
public class ReportAccessGuard {

    private final ReportRepository reportRepository;

    public Report requireAccessible(Long reportId, UserPrincipal requester) {
        if (requester.isManager()) {
            return reportRepository.findById(reportId)
                    .orElseThrow(() -> new ResourceNotFoundException("Report not found: " + reportId));
        }
        return reportRepository.findByIdAndUserId(reportId, requester.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Report not found: " + reportId));
    }
}
