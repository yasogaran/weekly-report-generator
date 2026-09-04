package com.company.weeklyreports.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * A registered account in the system - either a team member (submits reports)
 * or a manager (reviews reports, manages projects/users, views the dashboard).
 * This is the anchor entity for authentication and for every RBAC check.
 */
@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    // Unique because it also doubles as the login identifier.
    @Column(unique = true, nullable = false)
    private String email;

    // Never store or transmit a raw password - only the hashed value lives here.
    @Column(nullable = false)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    // Soft-delete flag, added for the User Management task: deactivating an
    // account must never hard-delete the row, since Report.user and
    // ReviewAction.reviewer both FK to User - removing the row would orphan
    // (or cascade-destroy, if cascading were configured) a former team
    // member's or manager's entire report/review history.
    @Column(nullable = false)
    @Builder.Default
    private boolean isActive = true;

    // Set once at insert time via the @PrePersist hook below; never updated afterwards.
    @Column(updatable = false)
    private LocalDateTime createdAt;

    // JPA lifecycle callback - fires right before the initial INSERT so callers
    // never need to remember to set createdAt themselves.
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
