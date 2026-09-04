package com.company.weeklyreports.repository;

import com.company.weeklyreports.model.dto.TimeByTypeDTO;
import com.company.weeklyreports.model.entity.HoursByType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

/**
 * Data access for HoursByType rows. Standard CRUD is always exercised via
 * the owning Report's cascade; the one extra method below is a dashboard
 * aggregate.
 */
public interface HoursByTypeRepository extends JpaRepository<HoursByType, Long> {

    // Time-by-type chart: total hours logged per task type, team-wide
    // (deliberately not scoped to any one member or report - this is a
    // company-wide time-allocation view). A straightforward single-entity
    // SUM/GROUP BY, since HoursByType is both the entity being summed and
    // the source of its own grouping key (taskType).
    @Query("""
            SELECT NEW com.company.weeklyreports.model.dto.TimeByTypeDTO(h.taskType, SUM(h.hours))
            FROM HoursByType h
            GROUP BY h.taskType
            ORDER BY SUM(h.hours) DESC
            """)
    List<TimeByTypeDTO> findTimeByType();
}
