package com.company.weeklyreports.repository;

import com.company.weeklyreports.model.entity.Project;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Data access for Projects. Managers get full CRUD via the base
 * JpaRepository methods; team members only need the active list below
 * when picking a project to tag a report with.
 */
public interface ProjectRepository extends JpaRepository<Project, Long> {

    // Feeds the report-creation project picker - soft-deleted (inactive)
    // projects stay in the table for historical reports but must not show
    // up as selectable options for new ones.
    List<Project> findByIsActiveTrue();
}
