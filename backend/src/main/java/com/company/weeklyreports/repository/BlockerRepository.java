package com.company.weeklyreports.repository;

import com.company.weeklyreports.model.entity.Blocker;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Data access for Blocker rows. Managed through the owning Report's
 * cascade, so plain JpaRepository CRUD is enough for now.
 */
public interface BlockerRepository extends JpaRepository<Blocker, Long> {
}
