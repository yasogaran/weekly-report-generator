package com.company.weeklyreports.repository;

import com.company.weeklyreports.model.entity.Achievement;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Data access for Achievement rows. Managed through the owning Report's
 * cascade, so plain JpaRepository CRUD is enough for now.
 */
public interface AchievementRepository extends JpaRepository<Achievement, Long> {
}
