package com.company.weeklyreports.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * A registered account — either a TEAM_MEMBER (files reports) or a MANAGER (reviews them,
 * sees the dashboard). Encapsulation is via Lombok's generated getters/setters over private
 * fields, not public fields directly; @Builder exists specifically for seed data / test
 * fixtures with several optional fields, per CLAUDE.md's builder-pattern guidance.
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

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String email;

    // Never serialized to a DTO — mapper/UserMapper.java deliberately has no path from
    // entity to this field.
    @Column(nullable = false)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    // Soft-delete flag for DELETE /users/{id} (api-doc.md) — blocks login and is re-checked
    // on every authenticated request (see security/JwtAuthFilter.java), never cascaded to
    // this user's existing reports/reviews.
    @Builder.Default
    @Column(nullable = false)
    private boolean isActive = true;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /** Stamps createdAt right before the first insert — simpler than a @PrePersist listener class for one field. */
    @jakarta.persistence.PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
