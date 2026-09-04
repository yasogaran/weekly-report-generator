package com.company.weeklyreports.service.impl;

import com.company.weeklyreports.exception.ConflictException;
import com.company.weeklyreports.exception.ResourceNotFoundException;
import com.company.weeklyreports.mapper.ReportMapper;
import com.company.weeklyreports.model.dto.ReportDTO;
import com.company.weeklyreports.model.dto.ReportRequest;
import com.company.weeklyreports.model.dto.ReportSummaryDTO;
import com.company.weeklyreports.model.entity.Project;
import com.company.weeklyreports.model.entity.Report;
import com.company.weeklyreports.model.entity.ReportStatus;
import com.company.weeklyreports.model.entity.User;
import com.company.weeklyreports.repository.ProjectRepository;
import com.company.weeklyreports.repository.ReportRepository;
import com.company.weeklyreports.repository.ReportSpecifications;
import com.company.weeklyreports.repository.UserRepository;
import com.company.weeklyreports.security.ReportAccessGuard;
import com.company.weeklyreports.security.UserPrincipal;
import com.company.weeklyreports.service.ReportService;
import com.company.weeklyreports.service.ReportStatusMachine;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ReportServiceImpl implements ReportService {

    private final ReportRepository reportRepository;
    private final UserRepository userRepository;
    private final ProjectRepository projectRepository;
    private final ReportAccessGuard accessGuard;
    private final ReportStatusMachine statusMachine;
    private final ReportMapper reportMapper;

    @Override
    @Transactional
    public ReportDTO create(UserPrincipal requester, ReportRequest request) {
        User user = userRepository.findById(requester.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + requester.getId()));
        Project project = projectRepository.findById(request.getProjectId())
                .orElseThrow(() -> new ResourceNotFoundException("Project not found: " + request.getProjectId()));

        Report report = Report.builder()
                .user(user)
                .project(project)
                .weekStartDate(request.getWeekStartDate())
                .weekEndDate(request.getWeekEndDate())
                .status(ReportStatus.DRAFT)
                .versionNumber(1)
                .notes(request.getNotes())
                .build();
        reportMapper.applyChildEntities(report, request);

        return reportMapper.toDto(reportRepository.save(report));
    }

    @Override
    @Transactional
    public ReportDTO update(Long id, UserPrincipal requester, ReportRequest request) {
        Report report = accessGuard.requireAccessible(id, requester);
        statusMachine.validateEditable(report.getStatus());

        // A NEEDS_CORRECTION report can only fork once. If this row already has a child
        // (i.e. it was already edited once before), its status is still NEEDS_CORRECTION in
        // the DB (forking doesn't change the parent's own status — it's just "frozen" by
        // convention, not by a status value), so validateEditable() alone wouldn't catch a
        // second edit attempt here. This explicit check is what actually enforces "frozen
        // from this point on" (api-doc.md, PATCH /reports/{id}).
        if (reportRepository.findByParentReportId(report.getId()).isPresent()) {
            throw new ConflictException("This version has already been superseded and can't be edited again.");
        }

        Project project = projectRepository.findById(request.getProjectId())
                .orElseThrow(() -> new ResourceNotFoundException("Project not found: " + request.getProjectId()));

        if (report.getStatus() == ReportStatus.DRAFT) {
            // In-place edit — same id, same version.
            report.setProject(project);
            report.setWeekStartDate(request.getWeekStartDate());
            report.setWeekEndDate(request.getWeekEndDate());
            report.setNotes(request.getNotes());
            reportMapper.applyChildEntities(report, request);
            return reportMapper.toDto(reportRepository.save(report));
        }

        // NEEDS_CORRECTION: fork into a brand-new row instead of editing this one in place
        // (system-design.md §4). `report` (the original) is left completely untouched from
        // here — not even its status changes — it simply becomes unreachable for further
        // edits via the check above once this new row exists as its child.
        Report fork = Report.builder()
                .user(report.getUser())
                .project(project)
                .weekStartDate(request.getWeekStartDate())
                .weekEndDate(request.getWeekEndDate())
                .status(ReportStatus.DRAFT)
                .versionNumber(report.getVersionNumber() + 1)
                .parentReport(report)
                .notes(request.getNotes())
                .build();
        reportMapper.applyChildEntities(fork, request);

        return reportMapper.toDto(reportRepository.save(fork));
    }

    @Override
    @Transactional
    public ReportDTO submit(Long id, UserPrincipal requester) {
        Report report = accessGuard.requireAccessible(id, requester);
        statusMachine.validateSubmittable(report.getStatus());

        report.setStatus(ReportStatus.SUBMITTED);
        report.setSubmittedAt(LocalDateTime.now());
        return reportMapper.toDto(reportRepository.save(report));
    }

    @Override
    public ReportDTO getById(Long id, UserPrincipal requester) {
        return reportMapper.toDto(accessGuard.requireAccessible(id, requester));
    }

    @Override
    public Page<ReportSummaryDTO> list(
            UserPrincipal requester, Pageable pageable, Long projectId, ReportStatus status, LocalDate week, Long memberId) {
        var spec = ReportSpecifications.build(requester.getId(), requester.isManager(), projectId, status, week, memberId);
        return reportRepository.findAll(spec, pageable).map(reportMapper::toSummaryDto);
    }

    @Override
    public List<ReportDTO> getVersions(Long id, UserPrincipal requester) {
        // Ownership checked once here, at whichever id was actually requested — every other
        // row in the chain inherits the same owner by construction (a fork always copies
        // report.getUser() from its parent, never accepts a different one), so no further
        // per-version ownership check is needed while walking the chain below.
        Report anchor = accessGuard.requireAccessible(id, requester);

        Report root = anchor;
        while (root.getParentReport() != null) {
            root = root.getParentReport();
        }

        List<Report> chain = new ArrayList<>();
        chain.add(root);
        Report current = root;
        while (true) {
            Optional<Report> child = reportRepository.findByParentReportId(current.getId());
            if (child.isEmpty()) {
                break;
            }
            current = child.get();
            chain.add(current);
        }

        // Newest first (api-doc.md) — the walk above builds oldest-first.
        List<ReportDTO> result = new ArrayList<>(chain.size());
        for (int i = chain.size() - 1; i >= 0; i--) {
            result.add(reportMapper.toDto(chain.get(i)));
        }
        return result;
    }
}
