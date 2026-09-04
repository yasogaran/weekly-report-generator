package com.company.weeklyreports.repository;

import com.company.weeklyreports.model.entity.Role;
import com.company.weeklyreports.model.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Data access for User accounts - backs login (lookup by email) and
 * registration (uniqueness check) in the auth flow.
 */
public interface UserRepository extends JpaRepository<User, Long> {

    // Login looks a user up by email (the login identifier), not by id.
    Optional<User> findByEmail(String email);

    // Cheaper than findByEmail().isPresent() during registration - avoids
    // pulling back the whole row just to check for a duplicate email.
    boolean existsByEmail(String email);

    // Denominator for the dashboard's complianceRate (added for the
    // Dashboard task): how many team members are currently expected to
    // submit a report at all. See DashboardService.getSummary() for the
    // full definition and its caveats.
    long countByRoleAndIsActiveTrue(Role role);
}
