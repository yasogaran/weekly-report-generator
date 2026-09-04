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
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * One manager decision on a report. Rows are only ever appended, never edited or deleted —
 * that's what gives GET /reports/{id}/reviews a full history "for free" (build-plan.md
 * Block 5) instead of needing a separate audit table. Attaches to the specific report
 * version it was left against, per api-doc.md's note that an older version's review history
 * doesn't include actions taken on a later fork.
 */
@Entity
@Table(name = "review_actions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReviewAction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "report_id", nullable = false)
    private Report report;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "reviewer_id", nullable = false)
    private User reviewer;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReviewActionType action;

    @Column(length = 2000)
    private String comment;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Only fills createdAt if the caller didn't already set one — a plain unconditional
     * assignment here would silently discard an explicitly backdated timestamp (e.g.
     * config/DataSeeder.java setting realistic historical review dates for demo data).
     */
    @PrePersist
    protected void onCreate() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
    }
}
