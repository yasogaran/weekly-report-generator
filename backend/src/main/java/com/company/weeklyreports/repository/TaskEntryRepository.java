package com.company.weeklyreports.repository;

import com.company.weeklyreports.model.entity.TaskEntry;
import org.springframework.data.jpa.repository.JpaRepository;

/** No custom finders yet — TaskEntry rows are only ever accessed through their parent Report. */
public interface TaskEntryRepository extends JpaRepository<TaskEntry, Long> {
}
