package com.company.weeklyreports.controller;

import com.company.weeklyreports.model.dto.ReportDTO;
import com.company.weeklyreports.model.dto.ReportRequest;
import com.company.weeklyreports.model.dto.ReportSummaryDTO;
import com.company.weeklyreports.model.entity.ReportStatus;
import com.company.weeklyreports.security.UserPrincipal;
import com.company.weeklyreports.service.ReportService;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Only report creation is a flat role check (@PreAuthorize) — everything else here is
 * ownership-gated inside ReportServiceImpl via ReportAccessGuard, since "is this your
 * report" isn't expressible as a static role annotation (api-doc.md's RBAC notes).
 */
@RestController
@RequestMapping("/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    @PostMapping
    @PreAuthorize("hasRole('TEAM_MEMBER')")
    public ReportDTO createReport(@AuthenticationPrincipal UserPrincipal principal, @Valid @RequestBody ReportRequest request) {
        return reportService.create(principal, request);
    }

    @PatchMapping("/{id}")
    public ReportDTO updateReport(
            @PathVariable Long id, @AuthenticationPrincipal UserPrincipal principal, @Valid @RequestBody ReportRequest request) {
        return reportService.update(id, principal, request);
    }

    @PostMapping("/{id}/submit")
    public ReportDTO submitReport(@PathVariable Long id, @AuthenticationPrincipal UserPrincipal principal) {
        return reportService.submit(id, principal);
    }

    @GetMapping("/{id}")
    public ReportDTO getReport(@PathVariable Long id, @AuthenticationPrincipal UserPrincipal principal) {
        return reportService.getById(id, principal);
    }

    @GetMapping
    public Page<ReportSummaryDTO> listReports(
            @AuthenticationPrincipal UserPrincipal principal,
            Pageable pageable,
            @RequestParam(required = false) Long projectId,
            @RequestParam(required = false) ReportStatus status,
            @RequestParam(required = false) LocalDate week,
            @RequestParam(required = false) Long memberId) {
        return reportService.list(principal, pageable, projectId, status, week, memberId);
    }

    @GetMapping("/{id}/versions")
    public List<ReportDTO> getVersions(@PathVariable Long id, @AuthenticationPrincipal UserPrincipal principal) {
        return reportService.getVersions(id, principal);
    }
}
