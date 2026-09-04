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

/**
 * One blocker entry on a report. At most one blocker per report should have
 * {@code isKeyIssue = true} — enforced in ReportServiceImpl when saving a report's blocker
 * list, not at the database level (a DB constraint can't express "at most one true per
 * report_id" without a partial/filtered unique index, which isn't worth it here).
 */
@Entity
@Table(name = "blockers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Blocker {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "report_id", nullable = false)
    private Report report;

    @Column(nullable = false, length = 1000)
    private String description;

    @Builder.Default
    @Column(nullable = false)
    private boolean isKeyIssue = false;
}
