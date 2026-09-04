package com.company.weeklyreports.repository;

import com.company.weeklyreports.model.entity.Project;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProjectRepository extends JpaRepository<Project, Long> {

    /** Backs GET /projects, which only ever returns active projects (api-doc.md). */
    List<Project> findByIsActiveTrue();
}
