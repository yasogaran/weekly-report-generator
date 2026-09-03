package com.company.weeklyreports.model.entity;

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
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * One task line item within a report - either work already done this week
 * (entryType = COMPLETED) or work planned for next week (PLANNED_NEXT_WEEK).
 * Both flavors share the same shape, so a single table/entity covers both
 * of the report's task sections rather than duplicating the fields.
 */
@Entity
@Table(name = "task_entries")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TaskEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Lazy: task entries are almost always loaded via the owning Report, so
    // fetching the parent report eagerly here would just re-fetch what the
    // caller already has.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "report_id", nullable = false)
    private Report report;

    @Column(nullable = false)
    private String taskName;

    private String priority;

    private Integer plannedPercent;

    private Integer actualPercent;

    // Free-text task status (e.g. "On Track", "Delayed") - not the report's
    // own lifecycle status, so it isn't the ReportStatus enum.
    private String status;

    private BigDecimal timePlanned;

    private BigDecimal timeSpent;

    private String deliverable;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EntryType entryType;
}
