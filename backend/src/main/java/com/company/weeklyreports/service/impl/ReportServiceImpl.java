package com.company.weeklyreports.service.impl;

import com.company.weeklyreports.exception.ReportNotEditableException;
import com.company.weeklyreports.exception.ResourceNotFoundException;
import com.company.weeklyreports.mapper.ReportMapper;
import com.company.weeklyreports.model.dto.CreateReportRequest;
import com.company.weeklyreports.model.dto.ReportDTO;
import com.company.weeklyreports.model.dto.ReportFilterParams;
import com.company.weeklyreports.model.dto.ReportSummaryDTO;
import com.company.weeklyreports.model.dto.UpdateReportRequest;
import com.company.weeklyreports.model.entity.Project;
import com.company.weeklyreports.model.entity.Report;
import com.company.weeklyreports.model.entity.Role;
import com.company.weeklyreports.model.entity.User;
import com.company.weeklyreports.repository.ProjectRepository;
import com.company.weeklyreports.repository.ReportRepository;
import com.company.weeklyreports.service.ReportAccessGuard;
import com.company.weeklyreports.service.ReportService;
import com.company.weeklyreports.service.ReportSpecifications;
import com.company.weeklyreports.service.ReportStateMachineService;
import com.company.weeklyreports.service.ReportTransitionAction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Implements the Report lifecycle: creation, editing (including the
 * NEEDS_CORRECTION versioning fork), submission, and RBAC-scoped reads.
 * Every read/write that touches a specific report id goes through an
 * ownership- or role-aware fetch first - see ReportAccessGuard - so
 * cross-member access is rejected at a single, consistent point shared
 * with ReviewServiceImpl, rather than re-implemented per method/service.
 */
@Service
public class ReportServiceImpl implements ReportService {

    private final ReportRepository reportRepository;
    private final ProjectRepository projectRepository;
    private final ReportStateMachineService reportStateMachineService;
    private final ReportAccessGuard reportAccessGuard;

    public ReportServiceImpl(ReportRepository reportRepository,
                              ProjectRepository projectRepository,
                              ReportStateMachineService reportStateMachineService,
                              ReportAccessGuard reportAccessGuard) {
        this.reportRepository = reportRepository;
        this.projectRepository = projectRepository;
        this.reportStateMachineService = reportStateMachineService;
        this.reportAccessGuard = reportAccessGuard;
    }

    // Builds a fresh report (version 1, DRAFT, no parent) owned by the
    // caller. projectId is resolved to a Project here (a service concern)
    // before handing both User and Project to the mapper.
    @Override
    @Transactional
    public ReportDTO createDraft(CreateReportRequest request, User currentUser) {
        Project project = findProjectOrThrow(request.getProjectId());
        Report report = ReportMapper.toEntity(request, currentUser, project);
        return ReportMapper.toDto(reportRepository.save(report));
    }

    // findByIdAndUserId both fetches the report AND enforces ownership in
    // one query - a team member editing a report that isn't theirs gets
    // exactly the same ResourceNotFoundException as editing an id that
    // doesn't exist at all.
    @Override
    @Transactional
    public ReportDTO updateReport(Long reportId, UpdateReportRequest request, User currentUser) {
        Report existing = reportRepository.findByIdAndUserId(reportId, currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Report not found: " + reportId));
        Project project = findProjectOrThrow(request.getProjectId());

        return switch (existing.getStatus()) {
            case DRAFT -> {
                // Nothing has been reviewed yet, so there's no history to
                // protect - editing the same row in place is safe.
                ReportMapper.applyToEntity(existing, request, project);
                yield ReportMapper.toDto(reportRepository.save(existing));
            }
            case NEEDS_CORRECTION -> {
                // A manager has already reviewed and left feedback against
                // THIS exact row's content ("report version history must be
                // preserved, not overwritten" per the assignment spec).
                // Mutating it here would silently rewrite what was
                // reviewed. So instead we fork: a brand-new row (with its
                // own fresh child entities, never shared with the old row)
                // carries the edit forward as the next version, and the old
                // row is left untouched forever, linked via parentReport so
                // it stays visible in the version history.
                Report forked = ReportMapper.toEntity(request, currentUser, project);
                forked.setVersionNumber(existing.getVersionNumber() + 1);
                forked.setParentReport(existing);
                yield ReportMapper.toDto(reportRepository.save(forked));
            }
            case SUBMITTED, APPROVED -> throw new ReportNotEditableException(existing.getStatus());
        };
    }

    // A plain state transition - no forking here, whether this DRAFT is a
    // true first version or a freshly-forked working copy makes no
    // difference to submission.
    @Override
    @Transactional
    public ReportDTO submitReport(Long reportId, User currentUser) {
        Report report = reportRepository.findByIdAndUserId(reportId, currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Report not found: " + reportId));

        var nextStatus = reportStateMachineService.transition(
                report.getStatus(), ReportTransitionAction.SUBMIT, Role.TEAM_MEMBER);

        report.setStatus(nextStatus);
        report.setSubmittedAt(LocalDateTime.now());
        return ReportMapper.toDto(reportRepository.save(report));
    }

    @Override
    @Transactional(readOnly = true)
    public ReportDTO getReportById(Long reportId, User currentUser) {
        return ReportMapper.toDto(reportAccessGuard.fetchAccessible(reportId, currentUser));
    }

    // Team members are always scoped to their own reports regardless of
    // what the incoming filters say - memberId is overridden, never merely
    // trusted, so a team member can't widen their own results by supplying
    // someone else's memberId.
    @Override
    @Transactional(readOnly = true)
    public Page<ReportSummaryDTO> listReports(ReportFilterParams filters, Pageable pageable, User currentUser) {
        ReportFilterParams effectiveFilters = filters == null ? ReportFilterParams.builder().build() : filters;
        if (currentUser.getRole() == Role.TEAM_MEMBER) {
            effectiveFilters = effectiveFilters.toBuilder().memberId(currentUser.getId()).build();
        }

        Specification<Report> spec = ReportSpecifications.fromFilters(effectiveFilters);
        return reportRepository.findAll(spec, pageable).map(ReportMapper::toSummaryDto);
    }

    // Walks the parentReport chain in both directions from the given id
    // (which may be any version, not necessarily the latest), then returns
    // every version found, newest first.
    @Override
    @Transactional(readOnly = true)
    public List<ReportDTO> getVersionHistory(Long reportId, User currentUser) {
        Report anchor = reportAccessGuard.fetchAccessible(reportId, currentUser);

        // Walk backward to the first version in the chain.
        Report earliest = anchor;
        while (earliest.getParentReport() != null) {
            earliest = earliest.getParentReport();
        }

        // Walk forward from the earliest version, following each report's
        // child (if any). A fork always assigns the same owner as its
        // parent, so the whole chain shares one owner - the ownership
        // check on the anchor above already covers every node here.
        List<Report> chain = new ArrayList<>();
        Report current = earliest;
        chain.add(current);
        while (true) {
            List<Report> children = reportRepository.findByParentReportId(current.getId());
            if (children.isEmpty()) {
                break;
            }
            current = children.get(0);
            chain.add(current);
        }

        Collections.reverse(chain);
        return chain.stream().map(ReportMapper::toDto).collect(Collectors.toList());
    }

    private Project findProjectOrThrow(Long projectId) {
        return projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found: " + projectId));
    }
}
