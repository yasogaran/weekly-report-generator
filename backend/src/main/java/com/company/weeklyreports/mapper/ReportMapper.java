package com.company.weeklyreports.mapper;

import com.company.weeklyreports.model.dto.AchievementDTO;
import com.company.weeklyreports.model.dto.BlockerDTO;
import com.company.weeklyreports.model.dto.HoursByTypeDTO;
import com.company.weeklyreports.model.dto.ReportDTO;
import com.company.weeklyreports.model.dto.ReportRequest;
import com.company.weeklyreports.model.dto.ReportSummaryDTO;
import com.company.weeklyreports.model.dto.TaskEntryDTO;
import com.company.weeklyreports.model.entity.Achievement;
import com.company.weeklyreports.model.entity.Blocker;
import com.company.weeklyreports.model.entity.HoursByType;
import com.company.weeklyreports.model.entity.Report;
import com.company.weeklyreports.model.entity.TaskEntry;
import org.springframework.stereotype.Component;

/**
 * Entity <-> DTO conversion for Report and its child collections, kept out of
 * ReportServiceImpl per CLAUDE.md's mapper pattern. Handles both directions: entity -> DTO
 * for responses, and request DTO -> entity for the child rows a create/update attaches to a
 * Report (see {@link #applyChildEntities}).
 */
@Component
public class ReportMapper {

    public ReportDTO toDto(Report report) {
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
                .taskEntries(report.getTaskEntries().stream().map(this::toTaskEntryDto).toList())
                .blockers(report.getBlockers().stream().map(this::toBlockerDto).toList())
                .achievements(report.getAchievements().stream().map(this::toAchievementDto).toList())
                .hoursByType(report.getHoursByType().stream().map(this::toHoursByTypeDto).toList())
                .createdAt(report.getCreatedAt())
                .updatedAt(report.getUpdatedAt())
                .submittedAt(report.getSubmittedAt())
                .build();
    }

    /** Lighter row shape for GET /reports — no nested collections need mapping at all. */
    public ReportSummaryDTO toSummaryDto(Report report) {
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

    private TaskEntryDTO toTaskEntryDto(TaskEntry entry) {
        return TaskEntryDTO.builder()
                .taskName(entry.getTaskName())
                .priority(entry.getPriority())
                .plannedPercent(entry.getPlannedPercent())
                .actualPercent(entry.getActualPercent())
                .status(entry.getStatus())
                .timePlanned(entry.getTimePlanned())
                .timeSpent(entry.getTimeSpent())
                .deliverable(entry.getDeliverable())
                .entryType(entry.getEntryType())
                .build();
    }

    private BlockerDTO toBlockerDto(Blocker blocker) {
        return BlockerDTO.builder()
                .description(blocker.getDescription())
                .isKeyIssue(blocker.isKeyIssue())
                .build();
    }

    private AchievementDTO toAchievementDto(Achievement achievement) {
        return AchievementDTO.builder()
                .description(achievement.getDescription())
                .isKeyAchievement(achievement.isKeyAchievement())
                .build();
    }

    private HoursByTypeDTO toHoursByTypeDto(HoursByType hours) {
        return HoursByTypeDTO.builder().taskType(hours.getTaskType()).hours(hours.getHours()).build();
    }

    /**
     * Replaces a report's entire child collections from a request — used by both create
     * (empty collections to start with) and in-place edit of a DRAFT (existing collections
     * cleared and rebuilt). Relies on Report's child lists having orphanRemoval=true, so
     * clearing them here actually deletes the old rows rather than leaving them orphaned.
     */
    public void applyChildEntities(Report report, ReportRequest request) {
        report.getTaskEntries().clear();
        for (TaskEntryDTO dto : request.getTaskEntries()) {
            report.addTaskEntry(TaskEntry.builder()
                    .taskName(dto.getTaskName())
                    .priority(dto.getPriority())
                    .plannedPercent(dto.getPlannedPercent())
                    .actualPercent(dto.getActualPercent())
                    .status(dto.getStatus())
                    .timePlanned(dto.getTimePlanned())
                    .timeSpent(dto.getTimeSpent())
                    .deliverable(dto.getDeliverable())
                    .entryType(dto.getEntryType())
                    .build());
        }

        report.getBlockers().clear();
        for (BlockerDTO dto : request.getBlockers()) {
            report.addBlocker(Blocker.builder()
                    .description(dto.getDescription())
                    .isKeyIssue(dto.isKeyIssue())
                    .build());
        }

        report.getAchievements().clear();
        for (AchievementDTO dto : request.getAchievements()) {
            report.addAchievement(Achievement.builder()
                    .description(dto.getDescription())
                    .isKeyAchievement(dto.isKeyAchievement())
                    .build());
        }

        report.getHoursByType().clear();
        for (HoursByTypeDTO dto : request.getHoursByType()) {
            report.addHoursByType(HoursByType.builder().taskType(dto.getTaskType()).hours(dto.getHours()).build());
        }
    }
}
