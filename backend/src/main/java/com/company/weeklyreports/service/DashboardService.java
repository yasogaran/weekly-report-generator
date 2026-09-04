package com.company.weeklyreports.service;

import com.company.weeklyreports.model.dto.ActivityFeedItemDTO;
import com.company.weeklyreports.model.dto.DashboardSummaryDTO;
import com.company.weeklyreports.model.dto.StatusByMemberDTO;
import com.company.weeklyreports.model.dto.TaskTrendPointDTO;
import com.company.weeklyreports.model.dto.TimeByTypeDTO;
import com.company.weeklyreports.model.dto.WorkloadByProjectDTO;
import com.company.weeklyreports.model.entity.ReportStatus;
import com.company.weeklyreports.model.entity.Role;
import com.company.weeklyreports.repository.BlockerRepository;
import com.company.weeklyreports.repository.HoursByTypeRepository;
import com.company.weeklyreports.repository.ReportRepository;
import com.company.weeklyreports.repository.ReviewActionRepository;
import com.company.weeklyreports.repository.TaskEntryRepository;
import com.company.weeklyreports.repository.UserRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Manager-facing aggregate metrics. Plain concrete class, no interface -
 * every method here is a thin pass-through to a repository aggregate query
 * (plus, for the summary and activity feed, a small amount of combining
 * logic over already-aggregated results) - there's no state machine or
 * versioning concern that would justify Dependency Inversion here.
 */
@Service
public class DashboardService {

    private final ReportRepository reportRepository;
    private final TaskEntryRepository taskEntryRepository;
    private final HoursByTypeRepository hoursByTypeRepository;
    private final ReviewActionRepository reviewActionRepository;
    private final BlockerRepository blockerRepository;
    private final UserRepository userRepository;

    public DashboardService(ReportRepository reportRepository,
                             TaskEntryRepository taskEntryRepository,
                             HoursByTypeRepository hoursByTypeRepository,
                             ReviewActionRepository reviewActionRepository,
                             BlockerRepository blockerRepository,
                             UserRepository userRepository) {
        this.reportRepository = reportRepository;
        this.taskEntryRepository = taskEntryRepository;
        this.hoursByTypeRepository = hoursByTypeRepository;
        this.reviewActionRepository = reviewActionRepository;
        this.blockerRepository = blockerRepository;
        this.userRepository = userRepository;
    }

    // Assembles the four summary numbers from four independent aggregate
    // queries (see field-by-field comments below) - there's no single
    // join that produces all four at once, since they span Report, User,
    // and Blocker.
    //
    // complianceRate definition (explicit business-logic assumption, not
    // obvious from the spec):
    //   numerator   = totalSubmittedThisWeek: reports ABOUT the current
    //                 week (weekStartDate = this week's Monday) that have
    //                 been submitted at least once (submittedAt IS NOT
    //                 NULL) - true for SUBMITTED, NEEDS_CORRECTION, and
    //                 APPROVED alike, since submittedAt is never cleared
    //                 once set.
    //   denominator = countByRoleAndIsActiveTrue(TEAM_MEMBER): every
    //                 currently-active team member is assumed to owe
    //                 exactly one report for the current week. This is an
    //                 approximation - someone deactivated or onboarded
    //                 mid-week would slightly skew the ratio - but the
    //                 data model has no separate "expected submitters"
    //                 concept, so "everyone currently active" is the
    //                 simplest defensible stand-in.
    //   0 active team members -> complianceRate is defined as 0.0 rather
    //                 than dividing by zero.
    @Transactional(readOnly = true)
    public DashboardSummaryDTO getSummary() {
        LocalDate weekStart = currentWeekStart();

        long totalSubmittedThisWeek = reportRepository.countByWeekStartDateAndSubmittedAtIsNotNull(weekStart);
        long activeTeamMembers = userRepository.countByRoleAndIsActiveTrue(Role.TEAM_MEMBER);
        double complianceRate = activeTeamMembers == 0
                ? 0.0
                : (double) totalSubmittedThisWeek / activeTeamMembers;

        long needsCorrectionCount = reportRepository.countByStatus(ReportStatus.NEEDS_CORRECTION);
        long openBlockersCount = blockerRepository.countByReport_StatusNot(ReportStatus.APPROVED);

        return DashboardSummaryDTO.builder()
                .totalSubmittedThisWeek(totalSubmittedThisWeek)
                .complianceRate(complianceRate)
                .needsCorrectionCount(needsCorrectionCount)
                .openBlockersCount(openBlockersCount)
                .build();
    }

    @Transactional(readOnly = true)
    public List<TaskTrendPointDTO> getTaskTrend() {
        return reportRepository.findTaskTrend();
    }

    @Transactional(readOnly = true)
    public List<StatusByMemberDTO> getStatusByMember() {
        return reportRepository.findStatusByMember();
    }

    @Transactional(readOnly = true)
    public List<WorkloadByProjectDTO> getWorkloadByProject() {
        return taskEntryRepository.findWorkloadByProject();
    }

    @Transactional(readOnly = true)
    public List<TimeByTypeDTO> getTimeByType() {
        return hoursByTypeRepository.findTimeByType();
    }

    // Merges two independently-sourced, already-database-limited top-N
    // lists (submissions from ReportRepository, reviews from
    // ReviewActionRepository) into one feed. This isn't the
    // fetch-everything-and-aggregate-in-Java pattern the task asked to
    // avoid: each sub-query already does its own ORDER BY + LIMIT at the
    // SQL level (via Pageable), so at most 2*limit rows ever reach the
    // JVM, and the only in-Java work is sorting that small, already-bounded
    // set by timestamp and truncating to limit. There's no single query
    // that could produce this directly, because no entity in this schema
    // represents a unified activity log spanning both Report submissions
    // and ReviewAction decisions - flagging this as a deliberate design
    // choice rather than a shortcut.
    @Transactional(readOnly = true)
    public List<ActivityFeedItemDTO> getActivityFeed(int limit) {
        Pageable topN = PageRequest.of(0, limit);

        List<ActivityFeedItemDTO> submissions = reportRepository.findRecentSubmissions(topN);
        List<ActivityFeedItemDTO> reviews = reviewActionRepository.findRecentReviewActivity(topN);

        return Stream.concat(submissions.stream(), reviews.stream())
                .sorted(Comparator.comparing(ActivityFeedItemDTO::getTimestamp).reversed())
                .limit(limit)
                .collect(Collectors.toList());
    }

    // "Current week" = Monday-Sunday containing today, on the assumption
    // that Report.weekStartDate is always populated with that week's
    // Monday (a convention enforced by the report-creation flow, not by a
    // database constraint - worth revisiting if that assumption ever
    // changes).
    private LocalDate currentWeekStart() {
        return LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
    }
}
