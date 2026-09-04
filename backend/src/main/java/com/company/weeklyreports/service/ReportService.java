package com.company.weeklyreports.service;

import com.company.weeklyreports.model.dto.ReportDTO;
import com.company.weeklyreports.model.dto.ReportRequest;
import com.company.weeklyreports.model.dto.ReportSummaryDTO;
import com.company.weeklyreports.model.entity.ReportStatus;
import com.company.weeklyreports.security.UserPrincipal;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Interface + Impl (Dependency Inversion) — this is the most complex service in the app
 * (versioning fork, state-machine-gated transitions, ownership enforcement), so it's the one
 * genuinely worth demonstrating the pattern on (CLAUDE.md; ProjectService/UserService stay
 * concrete since they're simple CRUD with no real benefit from an interface).
 */
public interface ReportService {

    /** POST /reports — always created as DRAFT, versionNumber 1, no parent. */
    ReportDTO create(UserPrincipal requester, ReportRequest request);

    /**
     * PATCH /reports/{id}. Ownership-checked inside (ReportAccessGuard), not @PreAuthorize.
     * See ReportServiceImpl for the versioning-fork behavior when the report is currently
     * NEEDS_CORRECTION — the returned DTO's id may differ from {@code id}.
     */
    ReportDTO update(Long id, UserPrincipal requester, ReportRequest request);

    /** POST /reports/{id}/submit. */
    ReportDTO submit(Long id, UserPrincipal requester);

    /** GET /reports/{id}. 404 (not 403) if it exists but isn't the requester's, unless they're a manager. */
    ReportDTO getById(Long id, UserPrincipal requester);

    /** GET /reports — paginated, filtered. Team members are always scoped to their own reports regardless of memberId. */
    Page<ReportSummaryDTO> list(
            UserPrincipal requester, Pageable pageable, Long projectId, ReportStatus status, LocalDate week, Long memberId);

    /** GET /reports/{id}/versions — full chain from any id in it, newest first. */
    List<ReportDTO> getVersions(Long id, UserPrincipal requester);
}
