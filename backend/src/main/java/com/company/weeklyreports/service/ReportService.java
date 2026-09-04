package com.company.weeklyreports.service;

import com.company.weeklyreports.model.dto.CreateReportRequest;
import com.company.weeklyreports.model.dto.ReportDTO;
import com.company.weeklyreports.model.dto.ReportFilterParams;
import com.company.weeklyreports.model.dto.ReportSummaryDTO;
import com.company.weeklyreports.model.dto.UpdateReportRequest;
import com.company.weeklyreports.model.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * Business logic for creating, editing, submitting, and reading Reports.
 * Interfaced (Dependency Inversion) because this is one of the two most
 * complex services in the system - the report lifecycle, versioning fork,
 * and RBAC-scoped reads all live behind this contract.
 */
public interface ReportService {

    // Creates a brand-new report (version 1, status DRAFT) owned by the caller.
    ReportDTO createDraft(CreateReportRequest request, User currentUser);

    // Edits an existing, owned report. Behavior depends on its current
    // status - see ReportServiceImpl for the DRAFT (in-place) vs
    // NEEDS_CORRECTION (fork) branches.
    ReportDTO updateReport(Long reportId, UpdateReportRequest request, User currentUser);

    // Moves an owned report from DRAFT/NEEDS_CORRECTION to SUBMITTED.
    ReportDTO submitReport(Long reportId, User currentUser);

    // Fetches one report by id, RBAC-scoped: managers can read any report,
    // team members only their own.
    ReportDTO getReportById(Long reportId, User currentUser);

    // Paginated, filterable report list - RBAC-scoped the same way as
    // getReportById (a team member's results are always their own reports,
    // regardless of what filters they pass).
    Page<ReportSummaryDTO> listReports(ReportFilterParams filters, Pageable pageable, User currentUser);

    // Every version in the correction chain that reportId belongs to,
    // newest first.
    List<ReportDTO> getVersionHistory(Long reportId, User currentUser);
}
