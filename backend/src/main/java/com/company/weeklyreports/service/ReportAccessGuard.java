package com.company.weeklyreports.service;

import com.company.weeklyreports.exception.ResourceNotFoundException;
import com.company.weeklyreports.model.entity.Report;
import com.company.weeklyreports.model.entity.Role;
import com.company.weeklyreports.model.entity.User;
import com.company.weeklyreports.repository.ReportRepository;
import org.springframework.stereotype.Component;

/**
 * Single shared fetch-and-authorize point for "get me report X, scoped to
 * who's asking": managers can access any report, team members only their
 * own. Extracted out of ReportServiceImpl so both ReportServiceImpl and
 * ReviewServiceImpl share one implementation of this rule - it's the core
 * IDOR check called out in rbac-matrix.md, and having two independent
 * copies would risk them drifting out of sync if the rule ever changes.
 */
@Component
public class ReportAccessGuard {

    private final ReportRepository reportRepository;

    public ReportAccessGuard(ReportRepository reportRepository) {
        this.reportRepository = reportRepository;
    }

    // Both branches throw the identical ResourceNotFoundException, so a
    // team member probing another member's report id can't distinguish
    // "doesn't exist" from "exists but isn't yours."
    public Report fetchAccessible(Long reportId, User currentUser) {
        if (currentUser.getRole() == Role.MANAGER) {
            return reportRepository.findById(reportId)
                    .orElseThrow(() -> new ResourceNotFoundException("Report not found: " + reportId));
        }
        return reportRepository.findByIdAndUserId(reportId, currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Report not found: " + reportId));
    }
}
