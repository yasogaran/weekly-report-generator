package com.company.weeklyreports.mapper;

import com.company.weeklyreports.model.dto.AchievementDTO;
import com.company.weeklyreports.model.dto.AchievementRequest;
import com.company.weeklyreports.model.dto.BlockerDTO;
import com.company.weeklyreports.model.dto.BlockerRequest;
import com.company.weeklyreports.model.dto.CreateReportRequest;
import com.company.weeklyreports.model.dto.HoursByTypeDTO;
import com.company.weeklyreports.model.dto.HoursByTypeRequest;
import com.company.weeklyreports.model.dto.ReportDTO;
import com.company.weeklyreports.model.dto.ReportSummaryDTO;
import com.company.weeklyreports.model.dto.TaskEntryDTO;
import com.company.weeklyreports.model.dto.TaskEntryRequest;
import com.company.weeklyreports.model.dto.UpdateReportRequest;
import com.company.weeklyreports.model.entity.Achievement;
import com.company.weeklyreports.model.entity.Blocker;
import com.company.weeklyreports.model.entity.HoursByType;
import com.company.weeklyreports.model.entity.Project;
import com.company.weeklyreports.model.entity.Report;
import com.company.weeklyreports.model.entity.ReportStatus;
import com.company.weeklyreports.model.entity.TaskEntry;
import com.company.weeklyreports.model.entity.User;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Converts between the Report aggregate (Report + its four child entity
 * lists) and its DTOs. Written by hand (no MapStruct) so every mapping
 * step, especially the child-list wiring, stays explicit and easy to
 * explain.
 */
public class ReportMapper {

    private ReportMapper() {
    }

    // Full detail view - includes all four child collections, so this is
    // only meant to be called for a single-report GET, never per-row in a
    // paginated list.
    public static ReportDTO toDto(Report report) {
        if (report == null) {
            return null;
        }
        return ReportDTO.builder()
                .id(report.getId())
                .userId(report.getUser().getId())
                .userName(report.getUser().getName())
                .projectId(report.getProject().getId())
                .projectName(report.getProject().getName())
                .weekStartDate(report.getWeekStartDate())
                .weekEndDate(report.getWeekEndDate())
                .status(report.getStatus())
                .versionNumber(report.getVersionNumber())
                .parentReportId(report.getParentReport() != null ? report.getParentReport().getId() : null)
                .notes(report.getNotes())
                .taskEntries(mapTaskEntries(report.getTaskEntries()))
                .blockers(mapBlockers(report.getBlockers()))
                .achievements(mapAchievements(report.getAchievements()))
                .hoursByType(mapHoursByType(report.getHoursByType()))
                .createdAt(report.getCreatedAt())
                .updatedAt(report.getUpdatedAt())
                .submittedAt(report.getSubmittedAt())
                .build();
    }

    // Lightweight view for list endpoints - no child collections, so
    // building a page of these never triggers lazy-loading of task
    // entries/blockers/achievements/hours for rows the caller isn't
    // actually looking at.
    public static ReportSummaryDTO toSummaryDto(Report report) {
        if (report == null) {
            return null;
        }
        return ReportSummaryDTO.builder()
                .id(report.getId())
                .userId(report.getUser().getId())
                .userName(report.getUser().getName())
                .projectName(report.getProject().getName())
                .weekStartDate(report.getWeekStartDate())
                .weekEndDate(report.getWeekEndDate())
                .status(report.getStatus())
                .updatedAt(report.getUpdatedAt())
                .build();
    }

    // User and Project are passed in rather than looked up by id here -
    // mappers don't talk to repositories. Resolving projectId to a Project
    // (and checking it's active) and resolving the authenticated caller to
    // a User are both service-layer concerns; this method only assembles
    // the entity graph once both are already in hand.
    //
    // A new report always starts at DRAFT / version 1 / no parent - those
    // only change via the submit and correction-cycle flows, not creation.
    public static Report toEntity(CreateReportRequest request, User user, Project project) {
        if (request == null) {
            return null;
        }
        return buildNewEntity(user, project, request.getWeekStartDate(), request.getWeekEndDate(),
                request.getNotes(), request.getTaskEntries(), request.getBlockers(),
                request.getAchievements(), request.getHoursByType());
    }

    // Same shape as the CreateReportRequest overload above - used by the
    // versioning fork in ReportService when a NEEDS_CORRECTION report is
    // edited. Builds an entirely new Report with its own fresh child
    // entities (never reusing the old report's rows), so the old row can
    // stay frozen as immutable history. versionNumber/parentReport are left
    // at their defaults here; ReportService sets them once the old report
    // (needed to compute versionNumber + 1 and the parent link) is in hand.
    public static Report toEntity(UpdateReportRequest request, User user, Project project) {
        if (request == null) {
            return null;
        }
        return buildNewEntity(user, project, request.getWeekStartDate(), request.getWeekEndDate(),
                request.getNotes(), request.getTaskEntries(), request.getBlockers(),
                request.getAchievements(), request.getHoursByType());
    }

    // In-place edit path: mutates an existing (already-persisted) Report's
    // own fields and replaces its child collections wholesale, rather than
    // building a new Report row. Used only when the report is still DRAFT -
    // nothing has been reviewed yet, so there's no history to protect and
    // overwriting in place is safe.
    public static void applyToEntity(Report existing, UpdateReportRequest request, Project project) {
        existing.setProject(project);
        existing.setWeekStartDate(request.getWeekStartDate());
        existing.setWeekEndDate(request.getWeekEndDate());
        existing.setNotes(request.getNotes());

        // clear() (backed by orphanRemoval = true on Report's collections)
        // deletes the old child rows on flush; re-adding fresh entities
        // wired to this same report is simpler and less error-prone than
        // diffing incoming vs. existing entries one by one.
        existing.getTaskEntries().clear();
        existing.getTaskEntries().addAll(toTaskEntryEntities(request.getTaskEntries(), existing));

        existing.getBlockers().clear();
        existing.getBlockers().addAll(toBlockerEntities(request.getBlockers(), existing));

        existing.getAchievements().clear();
        existing.getAchievements().addAll(toAchievementEntities(request.getAchievements(), existing));

        existing.getHoursByType().clear();
        existing.getHoursByType().addAll(toHoursByTypeEntities(request.getHoursByType(), existing));
    }

    // Shared construction logic behind both toEntity() overloads - a new
    // report always starts at DRAFT / version 1 / no parent, since those
    // only change afterward via the submit and correction-cycle flows.
    private static Report buildNewEntity(User user, Project project, LocalDate weekStartDate,
                                          LocalDate weekEndDate, String notes,
                                          List<TaskEntryRequest> taskEntries, List<BlockerRequest> blockers,
                                          List<AchievementRequest> achievements, List<HoursByTypeRequest> hoursByType) {
        Report report = Report.builder()
                .user(user)
                .project(project)
                .weekStartDate(weekStartDate)
                .weekEndDate(weekEndDate)
                .status(ReportStatus.DRAFT)
                .versionNumber(1)
                .notes(notes)
                .build();

        // Each child is built from its request and then explicitly wired
        // back to this report - the FK column (report_id) is driven by
        // this back-reference, not by list membership alone, so it has to
        // be set on every child or Hibernate would persist them with a
        // null report_id.
        report.getTaskEntries().addAll(toTaskEntryEntities(taskEntries, report));
        report.getBlockers().addAll(toBlockerEntities(blockers, report));
        report.getAchievements().addAll(toAchievementEntities(achievements, report));
        report.getHoursByType().addAll(toHoursByTypeEntities(hoursByType, report));

        return report;
    }

    private static List<TaskEntryDTO> mapTaskEntries(List<TaskEntry> entities) {
        if (entities == null) {
            return Collections.emptyList();
        }
        List<TaskEntryDTO> dtos = new ArrayList<>();
        for (TaskEntry entity : entities) {
            dtos.add(TaskEntryDTO.builder()
                    .id(entity.getId())
                    .taskName(entity.getTaskName())
                    .priority(entity.getPriority())
                    .plannedPercent(entity.getPlannedPercent())
                    .actualPercent(entity.getActualPercent())
                    .status(entity.getStatus())
                    .timePlanned(entity.getTimePlanned())
                    .timeSpent(entity.getTimeSpent())
                    .deliverable(entity.getDeliverable())
                    .entryType(entity.getEntryType())
                    .build());
        }
        return dtos;
    }

    private static List<Blocker> toBlockerEntities(List<BlockerRequest> requests, Report report) {
        List<Blocker> entities = new ArrayList<>();
        if (requests == null) {
            return entities;
        }
        for (BlockerRequest request : requests) {
            entities.add(Blocker.builder()
                    .report(report)
                    .description(request.getDescription())
                    .isKeyIssue(request.isKeyIssue())
                    .build());
        }
        return entities;
    }

    private static List<BlockerDTO> mapBlockers(List<Blocker> entities) {
        if (entities == null) {
            return Collections.emptyList();
        }
        List<BlockerDTO> dtos = new ArrayList<>();
        for (Blocker entity : entities) {
            dtos.add(BlockerDTO.builder()
                    .id(entity.getId())
                    .description(entity.getDescription())
                    .isKeyIssue(entity.isKeyIssue())
                    .build());
        }
        return dtos;
    }

    private static List<TaskEntry> toTaskEntryEntities(List<TaskEntryRequest> requests, Report report) {
        List<TaskEntry> entities = new ArrayList<>();
        if (requests == null) {
            return entities;
        }
        for (TaskEntryRequest request : requests) {
            entities.add(TaskEntry.builder()
                    .report(report)
                    .taskName(request.getTaskName())
                    .priority(request.getPriority())
                    .plannedPercent(request.getPlannedPercent())
                    .actualPercent(request.getActualPercent())
                    .status(request.getStatus())
                    .timePlanned(request.getTimePlanned())
                    .timeSpent(request.getTimeSpent())
                    .deliverable(request.getDeliverable())
                    .entryType(request.getEntryType())
                    .build());
        }
        return entities;
    }

    private static List<Achievement> toAchievementEntities(List<AchievementRequest> requests, Report report) {
        List<Achievement> entities = new ArrayList<>();
        if (requests == null) {
            return entities;
        }
        for (AchievementRequest request : requests) {
            entities.add(Achievement.builder()
                    .report(report)
                    .description(request.getDescription())
                    .isKeyAchievement(request.isKeyAchievement())
                    .build());
        }
        return entities;
    }

    private static List<AchievementDTO> mapAchievements(List<Achievement> entities) {
        if (entities == null) {
            return Collections.emptyList();
        }
        List<AchievementDTO> dtos = new ArrayList<>();
        for (Achievement entity : entities) {
            dtos.add(AchievementDTO.builder()
                    .id(entity.getId())
                    .description(entity.getDescription())
                    .isKeyAchievement(entity.isKeyAchievement())
                    .build());
        }
        return dtos;
    }

    private static List<HoursByType> toHoursByTypeEntities(List<HoursByTypeRequest> requests, Report report) {
        List<HoursByType> entities = new ArrayList<>();
        if (requests == null) {
            return entities;
        }
        for (HoursByTypeRequest request : requests) {
            entities.add(HoursByType.builder()
                    .report(report)
                    .taskType(request.getTaskType())
                    .hours(request.getHours())
                    .build());
        }
        return entities;
    }

    private static List<HoursByTypeDTO> mapHoursByType(List<HoursByType> entities) {
        if (entities == null) {
            return Collections.emptyList();
        }
        List<HoursByTypeDTO> dtos = new ArrayList<>();
        for (HoursByType entity : entities) {
            dtos.add(HoursByTypeDTO.builder()
                    .id(entity.getId())
                    .taskType(entity.getTaskType())
                    .hours(entity.getHours())
                    .build());
        }
        return dtos;
    }
}
