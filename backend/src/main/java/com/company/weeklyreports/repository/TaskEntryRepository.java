package com.company.weeklyreports.repository;

import com.company.weeklyreports.model.entity.TaskEntry;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Data access for TaskEntry rows. These are always created/read/deleted as
 * part of their owning Report (via cascade), so no extra query methods are
 * needed yet beyond the standard JpaRepository CRUD.
 */
public interface TaskEntryRepository extends JpaRepository<TaskEntry, Long> {
}
