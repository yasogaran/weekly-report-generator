package com.company.weeklyreports.repository;

import com.company.weeklyreports.model.dto.WorkloadByProjectDTO;
import com.company.weeklyreports.model.entity.TaskEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

/**
 * Data access for TaskEntry rows. Standard CRUD is always exercised via the
 * owning Report's cascade; the one extra method below is a dashboard
 * aggregate.
 */
public interface TaskEntryRepository extends JpaRepository<TaskEntry, Long> {

    // Workload-by-project chart: task count per project. Lives here (not
    // ReportRepository or ProjectRepository) because TaskEntry is what's
    // actually being counted - "workload" is measured in tasks, not
    // reports - even though the grouping key (project name) requires
    // walking two hops up (TaskEntry -> Report -> Project).
    @Query("""
            SELECT NEW com.company.weeklyreports.model.dto.WorkloadByProjectDTO(p.id, p.name, COUNT(t))
            FROM TaskEntry t JOIN t.report r JOIN r.project p
            GROUP BY p.id, p.name
            ORDER BY COUNT(t) DESC
            """)
    List<WorkloadByProjectDTO> findWorkloadByProject();
}
