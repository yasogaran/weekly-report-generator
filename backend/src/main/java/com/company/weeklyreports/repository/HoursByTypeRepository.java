package com.company.weeklyreports.repository;

import com.company.weeklyreports.model.entity.HoursByType;
import org.springframework.data.jpa.repository.JpaRepository;

/** No custom finders yet — HoursByType rows are only ever accessed through their parent Report. */
public interface HoursByTypeRepository extends JpaRepository<HoursByType, Long> {
}
