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
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * A single team member's weekly report for one project/week. This is the
 * aggregate root for a week's worth of task entries, blockers, achievements
 * and hour breakdowns - all four child collections are owned by the report
 * and are persisted/removed together with it.
 *
 * Versioning: a correction cycle never edits a submitted report in place.
 * Instead a brand-new Report row is created with parentReport pointing back
 * to the report it supersedes and versionNumber incremented, so the full
 * history stays queryable (see docs/system-design.md section 4).
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

    // Lazy - list/dashboard queries pull reports in bulk and don't always
    // need the full owning User loaded.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    private LocalDate weekStartDate;

    private LocalDate weekEndDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReportStatus status;

    @Column(nullable = false)
    @Builder.Default
    private int versionNumber = 1;

    // Self-referencing FK: points at the report this one was resubmitted
    // from after a correction request. Null for a report's first version.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_report_id")
    private Report parentReport;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    // Null until the member actually submits (as opposed to saving a draft).
    private LocalDateTime submittedAt;

    // Owned collections: cascade = ALL + orphanRemoval means deleting a
    // report (or removing an item from these lists) removes the matching
    // child rows too - callers never manage TaskEntry/Blocker/etc. rows
    // through their own repositories independent of the parent report.
    @OneToMany(mappedBy = "report", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<TaskEntry> taskEntries = new ArrayList<>();

    @OneToMany(mappedBy = "report", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Blocker> blockers = new ArrayList<>();

    @OneToMany(mappedBy = "report", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Achievement> achievements = new ArrayList<>();

    @OneToMany(mappedBy = "report", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<HoursByType> hoursByType = new ArrayList<>();

    // Stamps createdAt/updatedAt together on first insert so both are
    // consistent for a brand-new draft.
    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    // Keeps updatedAt current on every save (draft edits, resubmits, status
    // changes made by the manager's review action).
    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
