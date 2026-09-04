package com.company.weeklyreports.repository;

import com.company.weeklyreports.model.entity.Blocker;
import com.company.weeklyreports.model.entity.ReportStatus;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Data access for Blocker rows. Standard CRUD is always exercised via the
 * owning Report's cascade; the one extra method below is a dashboard
 * aggregate.
 */
public interface BlockerRepository extends JpaRepository<Blocker, Long> {

    // DashboardSummaryDTO.openBlockersCount (added for the Dashboard task -
    // not one of the four repositories that task explicitly named, but
    // this metric has nowhere else to live). Blocker has no resolved/status
    // field of its own, so "open" is defined here as "belongs to a report
    // that hasn't reached APPROVED yet" - once a report is approved, its
    // blockers are treated as resolved by implication, since the reporting
    // cycle for that week is finalized. This was an explicit assumption
    // call, confirmed before implementing.
    long countByReport_StatusNot(ReportStatus status);
}
