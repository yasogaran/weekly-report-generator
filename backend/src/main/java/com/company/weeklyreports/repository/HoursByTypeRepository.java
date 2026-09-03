package com.company.weeklyreports.repository;

import com.company.weeklyreports.model.entity.HoursByType;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Data access for HoursByType rows. Managed through the owning Report's
 * cascade, so plain JpaRepository CRUD is enough for now.
 */
public interface HoursByTypeRepository extends JpaRepository<HoursByType, Long> {
}
