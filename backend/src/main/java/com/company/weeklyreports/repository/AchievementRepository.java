package com.company.weeklyreports.repository;

import com.company.weeklyreports.model.entity.Achievement;
import org.springframework.data.jpa.repository.JpaRepository;

/** No custom finders yet — Achievement rows are only ever accessed through their parent Report. */
public interface AchievementRepository extends JpaRepository<Achievement, Long> {
}
