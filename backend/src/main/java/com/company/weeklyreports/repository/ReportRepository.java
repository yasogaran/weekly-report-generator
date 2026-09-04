package com.company.weeklyreports.repository;

import com.company.weeklyreports.model.entity.Report;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

/**
 * JpaSpecificationExecutor backs GET /reports' optional week/projectId/status/memberId
 * filters (api-doc.md) — a Specification composes only the filters actually supplied,
 * which is a cleaner fit here than a wall of "if param != null" branches or a combinatorial
 * explosion of derived-query method names for every filter combination.
 */
public interface ReportRepository extends JpaRepository<Report, Long>, JpaSpecificationExecutor<Report> {

    Optional<Report> findByIdAndUserId(Long id, Long userId);

    /**
     * Walking forward one step in a version chain (system-design.md §4). Each report forks
     * into at most one child (a report is frozen the moment it forks — see
     * ReportServiceImpl), so this is always a linear walk, never a branch.
     */
    Optional<Report> findByParentReportId(Long parentReportId);
}
