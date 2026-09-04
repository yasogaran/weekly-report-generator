package com.company.weeklyreports.repository;

import com.company.weeklyreports.model.entity.User;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/** Spring Data generates the implementation at runtime from these method names — no manual SQL. */
public interface UserRepository extends JpaRepository<User, Long> {

    /** Used by login and registration's duplicate-email check. */
    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);
}
