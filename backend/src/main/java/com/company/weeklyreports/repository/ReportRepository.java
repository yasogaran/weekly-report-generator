package com.company.weeklyreports.repository;

import com.company.weeklyreports.model.entity.Report;
import com.company.weeklyreports.model.entity.ReportStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Data access for Reports - the busiest repository in the system, since
 * nearly every list/detail endpoint (member history, manager dashboard,
 * review queue) reads through here with pagination and RBAC filtering.
 */
public interface ReportRepository extends JpaRepository<Report, Long> {

    // A team member's own report history, paginated - used both for the
    // member's "my history" page and as a building block for manager views
    // filtered down to one member.
    Page<Report> findByUserId(Long userId, Pageable pageable);

    // Manager-facing queue views (e.g. "all reports awaiting review") and
    // dashboard filters, filtered by lifecycle status.
    Page<Report> findByStatus(ReportStatus status, Pageable pageable);

    // Ownership-safe single-report lookup: matches on id AND userId in one
    // query so a team member can never fetch another member's report by
    // guessing/incrementing the id (the core IDOR test case from
    // rbac-matrix.md) - no separate "fetch then compare owner" step needed.
    Optional<Report> findByIdAndUserId(Long id, Long userId);
}
