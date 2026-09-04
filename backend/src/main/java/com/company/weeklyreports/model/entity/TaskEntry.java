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

/**
 * One row of a report's task table — reused for both the "completed" and "planned next
 * week" sections, distinguished only by {@link #entryType}, matching how the frontend keeps
 * these in a single array too (see frontend lib/reportTypes.ts).
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

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "report_id", nullable = false)
    private Report report;

    @Column(nullable = false)
    private String taskName;

    private String priority;

    @Column(nullable = false)
    private Integer plannedPercent;

    @Column(nullable = false)
    private Integer actualPercent;

    private String status;

    private Double timePlanned;

    private Double timeSpent;

    @Column(length = 1000)
    private String deliverable;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TaskEntryType entryType;
}
