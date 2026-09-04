package com.company.weeklyreports.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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

/** Optional hours-by-task-type breakdown row (e.g. "Development: 6 hours"). */
@Entity
@Table(name = "hours_by_type")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HoursByType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "report_id", nullable = false)
    private Report report;

    @Column(nullable = false)
    private String taskType;

    @Column(nullable = false)
    private Double hours;
}
