package com.company.weeklyreports.model.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * One weekly report. Self-referencing via {@code parentReport} to form a version chain
 * (system-design.md §4): resubmitting from NEEDS_CORRECTION creates a brand-new row pointing
 * back at this one, rather than overwriting it — this row is then frozen (see
 * ReportServiceImpl for where that's enforced).
 * <p>
 * Owns TaskEntry/Blocker/Achievement/HoursByType with cascade + orphanRemoval: these child
 * rows only ever exist as part of a report, so deleting/replacing them here is exactly
 * "this report's edit," never a separate lifecycle.
 */
@Entity
@Table(name = "reports")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Report {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @Column(nullable = false)
    private LocalDate weekStartDate;

    @Column(nullable = false)
    private LocalDate weekEndDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReportStatus status;

    @Builder.Default
    @Column(nullable = false)
    private Integer versionNumber = 1;

    // Self-referencing FK for the version chain (system-design.md §4). Null for a version-1
    // report; set to the report it superseded for every fork after that.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_report_id")
    private Report parentReport;

    @Column(length = 2000)
    private String notes;

    @OneToMany(mappedBy = "report", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<TaskEntry> taskEntries = new ArrayList<>();

    @OneToMany(mappedBy = "report", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<Blocker> blockers = new ArrayList<>();

    @OneToMany(mappedBy = "report", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<Achievement> achievements = new ArrayList<>();

    @OneToMany(mappedBy = "report", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<HoursByType> hoursByType = new ArrayList<>();

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    private LocalDateTime submittedAt;

    /** Stamps createdAt/updatedAt on first insert. */
    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    /** Keeps updatedAt current on every save — used by the history list's "last updated" column. */
    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    /** Convenience used by child-entity setters below to keep both sides of the relationship in sync. */
    public void addTaskEntry(TaskEntry entry) {
        taskEntries.add(entry);
        entry.setReport(this);
    }

    public void addBlocker(Blocker blocker) {
        blockers.add(blocker);
        blocker.setReport(this);
    }

    public void addAchievement(Achievement achievement) {
        achievements.add(achievement);
        achievement.setReport(this);
    }

    public void addHoursByType(HoursByType hours) {
        hoursByType.add(hours);
        hours.setReport(this);
    }
}
