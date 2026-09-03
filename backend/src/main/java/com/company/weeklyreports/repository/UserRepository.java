package com.company.weeklyreports.repository;

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
}
