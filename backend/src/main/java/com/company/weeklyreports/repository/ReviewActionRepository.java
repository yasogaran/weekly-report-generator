package com.company.weeklyreports.repository;

import com.company.weeklyreports.model.entity.ReviewAction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Data access for ReviewAction rows - the audit trail of manager decisions
 * on a report (approve / request changes).
 */
public interface ReviewActionRepository extends JpaRepository<ReviewAction, Long> {

    // Full review history for one report, newest first - used on the
    // review page so a manager can see prior rounds of feedback alongside
    // the version currently under review.
    List<ReviewAction> findByReport_IdOrderByCreatedAtDesc(Long reportId);
}
