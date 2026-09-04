package com.company.weeklyreports.repository;

import com.company.weeklyreports.model.entity.Blocker;
import org.springframework.data.jpa.repository.JpaRepository;

/** No custom finders yet — Blocker rows are only ever accessed through their parent Report. */
public interface BlockerRepository extends JpaRepository<Blocker, Long> {
}
