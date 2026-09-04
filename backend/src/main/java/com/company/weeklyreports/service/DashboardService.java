package com.company.weeklyreports.service;

import com.company.weeklyreports.model.dto.ActivityFeedItemDTO;
import com.company.weeklyreports.model.dto.DashboardSummaryDTO;
import com.company.weeklyreports.model.dto.StatusByMemberDTO;
import com.company.weeklyreports.model.dto.TaskTrendPointDTO;
import com.company.weeklyreports.model.dto.TimeByTypeDTO;
import com.company.weeklyreports.model.dto.WorkloadByProjectDTO;
import com.company.weeklyreports.model.entity.Blocker;
import com.company.weeklyreports.model.entity.HoursByType;
import com.company.weeklyreports.model.entity.Report;
import com.company.weeklyreports.model.entity.ReportStatus;
import com.company.weeklyreports.model.entity.ReviewActionType;
import com.company.weeklyreports.model.entity.Role;
import com.company.weeklyreports.model.entity.TaskEntry;
import com.company.weeklyreports.model.entity.TaskEntryType;
import com.company.weeklyreports.repository.BlockerRepository;
import com.company.weeklyreports.repository.HoursByTypeRepository;
import com.company.weeklyreports.repository.ReportRepository;
import com.company.weeklyreports.repository.ReviewActionRepository;
import com.company.weeklyreports.repository.TaskEntryRepository;
import com.company.weeklyreports.repository.UserRepository;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Plain concrete class (CLAUDE.md — not one of the two services that warrant an interface).
 * <p>
 * IMPORTANT — business-rule assumptions: api-doc.md gives the field names and an example
 * shape for each of these endpoints but not the exact calculation rules behind them. The
 * choices below are documented per-method rather than guessed silently:
 * <ul>
 *   <li>"This week" = the current ISO week, Monday-start, computed from the server clock.</li>
 *   <li>A report counts as "submitted this week" if its weekStartDate is the current week's
 *       Monday AND it has actually left DRAFT (SUBMITTED/NEEDS_CORRECTION/APPROVED) — a
 *       still-unsaved DRAFT for this week isn't "submitted."</li>
 *   <li>complianceRate = (members who submitted this week) / (active TEAM_MEMBER count).</li>
 *   <li>"Open" blockers = Blocker rows on a report still in the active review cycle
 *       (SUBMITTED or NEEDS_CORRECTION) — there's no isResolved flag on Blocker, so a
 *       blocker on an APPROVED report is treated as resolved-by-approval, not "open."</li>
 * </ul>
 * All of this pulls full tables into memory and aggregates with streams rather than
 * database-side GROUP BY queries — acceptable for this project's scope (a handful of users/
 * reports), not something to do this way against a large dataset.
 */
@Service
@RequiredArgsConstructor
public class DashboardService {

    private final ReportRepository reportRepository;
    private final TaskEntryRepository taskEntryRepository;
    private final HoursByTypeRepository hoursByTypeRepository;
    private final BlockerRepository blockerRepository;
    private final UserRepository userRepository;
    private final ReviewActionRepository reviewActionRepository;

    private LocalDate currentWeekStart() {
        return LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
    }

    public DashboardSummaryDTO getSummary() {
        LocalDate weekStart = currentWeekStart();
        List<Report> allReports = reportRepository.findAll();

        long submittedThisWeek = allReports.stream()
                .filter(r -> weekStart.equals(r.getWeekStartDate()))
                .filter(r -> r.getStatus() != ReportStatus.DRAFT)
                .map(r -> r.getUser().getId())
                .distinct()
                .count();

        long activeMemberCount = userRepository.findAll().stream()
                .filter(u -> u.getRole() == Role.TEAM_MEMBER && u.isActive())
                .count();
        double complianceRate = activeMemberCount == 0 ? 0.0 : (double) submittedThisWeek / activeMemberCount;

        long needsCorrectionCount =
                allReports.stream().filter(r -> r.getStatus() == ReportStatus.NEEDS_CORRECTION).count();

        long openBlockersCount = blockerRepository.findAll().stream()
                .filter(b -> b.getReport().getStatus() == ReportStatus.SUBMITTED
                        || b.getReport().getStatus() == ReportStatus.NEEDS_CORRECTION)
                .count();

        return DashboardSummaryDTO.builder()
                .totalSubmittedThisWeek(submittedThisWeek)
                .complianceRate(complianceRate)
                .needsCorrectionCount(needsCorrectionCount)
                .openBlockersCount(openBlockersCount)
                .build();
    }

    public List<TaskTrendPointDTO> getTaskTrend() {
        List<TaskEntry> completed = taskEntryRepository.findAll().stream()
                .filter(t -> t.getEntryType() == TaskEntryType.COMPLETED)
                .toList();

        Map<LocalDate, Long> byWeek = completed.stream()
                .collect(Collectors.groupingBy(
                        t -> t.getReport().getWeekStartDate(), Collectors.counting()));

        return byWeek.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(e -> TaskTrendPointDTO.builder().weekStartDate(e.getKey()).completedTaskCount(e.getValue()).build())
                .toList();
    }

    public List<StatusByMemberDTO> getStatusByMember() {
        record Key(Long userId, String userName, ReportStatus status) {
        }

        Map<Key, Long> counts = reportRepository.findAll().stream()
                .collect(Collectors.groupingBy(
                        r -> new Key(r.getUser().getId(), r.getUser().getName(), r.getStatus()),
                        Collectors.counting()));

        return counts.entrySet().stream()
                .map(e -> StatusByMemberDTO.builder()
                        .userId(e.getKey().userId())
                        .userName(e.getKey().userName())
                        .status(e.getKey().status())
                        .count(e.getValue())
                        .build())
                .sorted(Comparator.comparing(StatusByMemberDTO::getUserName))
                .toList();
    }

    public List<WorkloadByProjectDTO> getWorkloadByProject() {
        record Key(Long projectId, String projectName) {
        }

        Map<Key, Long> counts = taskEntryRepository.findAll().stream()
                .collect(Collectors.groupingBy(
                        t -> new Key(t.getReport().getProject().getId(), t.getReport().getProject().getName()),
                        Collectors.counting()));

        return counts.entrySet().stream()
                .map(e -> WorkloadByProjectDTO.builder()
                        .projectId(e.getKey().projectId())
                        .projectName(e.getKey().projectName())
                        .taskCount(e.getValue())
                        .build())
                .sorted(Comparator.comparing(WorkloadByProjectDTO::getProjectName))
                .toList();
    }

    public List<TimeByTypeDTO> getTimeByType() {
        Map<String, Double> totals = hoursByTypeRepository.findAll().stream()
                .collect(Collectors.groupingBy(HoursByType::getTaskType, Collectors.summingDouble(HoursByType::getHours)));

        return totals.entrySet().stream()
                .map(e -> TimeByTypeDTO.builder().taskType(e.getKey()).totalHours(e.getValue()).build())
                .sorted(Comparator.comparing(TimeByTypeDTO::getTaskType))
                .toList();
    }

    /**
     * Synthesized from two sources rather than a dedicated activity-log table: every
     * report's own submittedAt (a SUBMITTED event) and every ReviewAction row (an APPROVED
     * or REQUESTED_CHANGES event) — merged and sorted by timestamp, most recent first.
     */
    public List<ActivityFeedItemDTO> getActivityFeed() {
        List<ActivityFeedItemDTO> submissions = reportRepository.findAll().stream()
                .filter(r -> r.getSubmittedAt() != null)
                .map(r -> ActivityFeedItemDTO.builder()
                        .type("SUBMITTED")
                        .reportId(r.getId())
                        .userName(r.getUser().getName())
                        .timestamp(r.getSubmittedAt())
                        .detail("submitted a report for " + r.getWeekStartDate() + " to " + r.getWeekEndDate())
                        .build())
                .toList();

        List<ActivityFeedItemDTO> reviews = reviewActionRepository.findAll().stream()
                .map(a -> ActivityFeedItemDTO.builder()
                        .type(a.getAction().name())
                        .reportId(a.getReport().getId())
                        .userName(a.getReport().getUser().getName())
                        .timestamp(a.getCreatedAt())
                        .detail(a.getAction() == ReviewActionType.APPROVED
                                ? "'s report was approved"
                                : "was asked to make changes: " + a.getComment())
                        .build())
                .toList();

        return java.util.stream.Stream.concat(submissions.stream(), reviews.stream())
                .sorted(Comparator.comparing(ActivityFeedItemDTO::getTimestamp).reversed())
                .toList();
    }
}
