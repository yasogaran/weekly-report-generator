package com.company.weeklyreports.repository;

import com.company.weeklyreports.model.dto.ActivityFeedItemDTO;
import com.company.weeklyreports.model.dto.StatusByMemberDTO;
import com.company.weeklyreports.model.dto.TaskTrendPointDTO;
import com.company.weeklyreports.model.entity.Report;
import com.company.weeklyreports.model.entity.ReportStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Data access for Reports - the busiest repository in the system, since
 * nearly every list/detail endpoint (member history, manager dashboard,
 * review queue) reads through here with pagination and RBAC filtering.
 *
 * Also extends JpaSpecificationExecutor: the manager's list view can filter
 * by any combination of memberId/projectId/status/week range at once, and a
 * derived-method-per-combination approach would need one method per subset
 * of filters. findAll(Specification, Pageable) lets ReportServiceImpl build
 * a single dynamic query instead.
 */
public interface ReportRepository extends JpaRepository<Report, Long>, JpaSpecificationExecutor<Report> {

    // A team member's own report history, paginated - used both for the
    // member's "my history" page and as a building block for manager views
    // filtered down to one member.
    Page<Report> findByUserId(Long userId, Pageable pageable);

    // Manager-facing queue views (e.g. "all reports awaiting review") and
    // dashboard filters, filtered by lifecycle status.
    Page<Report> findByStatus(ReportStatus status, Pageable pageable);

    // Ownership-safe single-report lookup: matches on id AND userId in one
    // query so a team member can never fetch another member's report by
    // guessing/incrementing the id (the core IDOR test case from
    // rbac-matrix.md) - no separate "fetch then compare owner" step needed.
    Optional<Report> findByIdAndUserId(Long id, Long userId);

    // The report(s) created by forking this one during a correction cycle -
    // used to walk a version chain forward from an older version toward the
    // latest, since GET /reports/{id}/versions can be called with any
    // version's id, not just the newest one.
    List<Report> findByParentReportId(Long parentReportId);

    // --- Dashboard aggregates below (added for the Dashboard task) ---

    // Numerator for the dashboard's complianceRate, and
    // DashboardSummaryDTO.totalSubmittedThisWeek directly: reports ABOUT
    // the given week (weekStartDate match) that have been submitted at
    // least once. submittedAt is set on first submission and never
    // cleared afterward (see ReportServiceImpl.submitReport), so this is
    // true for SUBMITTED, NEEDS_CORRECTION, and APPROVED reports alike -
    // a plain COUNT query, no fetch-and-filter.
    long countByWeekStartDateAndSubmittedAtIsNotNull(LocalDate weekStartDate);

    // DashboardSummaryDTO.needsCorrectionCount: the current team-wide
    // backlog of reports awaiting a correction, unscoped by week (a report
    // stuck in NEEDS_CORRECTION stays a backlog item regardless of which
    // week it was originally about).
    long countByStatus(ReportStatus status);

    // Task-trend chart: completed-task count grouped by the week the
    // report was about. Lives here (not TaskEntryRepository) because the
    // grouping key - weekStartDate - is a Report field, even though the
    // rows being counted are TaskEntry children; Report is the natural
    // query root for "group by week."
    @Query("""
            SELECT NEW com.company.weeklyreports.model.dto.TaskTrendPointDTO(r.weekStartDate, COUNT(t))
            FROM Report r JOIN r.taskEntries t
            WHERE t.entryType = com.company.weeklyreports.model.entity.EntryType.COMPLETED
            GROUP BY r.weekStartDate
            ORDER BY r.weekStartDate ASC
            """)
    List<TaskTrendPointDTO> findTaskTrend();

    // Status-by-member chart: how many reports each member currently has
    // in each status. Both the grouping keys (user, status) are plain
    // Report fields, so this is a single-entity aggregate with no join
    // beyond the implicit r.user path expression.
    @Query("""
            SELECT NEW com.company.weeklyreports.model.dto.StatusByMemberDTO(r.user.id, r.user.name, r.status, COUNT(r))
            FROM Report r
            GROUP BY r.user.id, r.user.name, r.status
            ORDER BY r.user.name, r.status
            """)
    List<StatusByMemberDTO> findStatusByMember();

    // Activity feed, submission half: the most recent N first-or-repeat
    // submissions. Paired in DashboardService with
    // ReviewActionRepository.findRecentReviewActivity() to build the full
    // feed - see DashboardService.getActivityFeed() for why this can't be
    // a single query (there is no unified activity-log entity spanning
    // both Report and ReviewAction). The Pageable here becomes a real
    // LIMIT/OFFSET at the SQL level, not an in-memory truncation.
    @Query("""
            SELECT NEW com.company.weeklyreports.model.dto.ActivityFeedItemDTO(
                'SUBMITTED', r.id, r.user.name, r.submittedAt, null)
            FROM Report r
            WHERE r.submittedAt IS NOT NULL
            ORDER BY r.submittedAt DESC
            """)
    List<ActivityFeedItemDTO> findRecentSubmissions(Pageable pageable);
}
