package com.company.weeklyreports.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * A project/category that reports are tagged against. Managers own CRUD on
 * this entity; team members only read it (to pick a project when filing a
 * report). Deletion is a soft-delete via isActive rather than a hard delete,
 * so historical reports keep a valid project reference.
 */
@Entity
@Table(name = "projects")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Project {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    private String description;

    // Soft-delete flag - inactive projects are hidden from new-report project
    // pickers but stay intact for reports that already reference them.
    @Column(nullable = false)
    @Builder.Default
    private boolean isActive = true;

    // Lazy: we rarely need the creating manager's full User record just to
    // display a project, so avoid the extra join unless explicitly fetched.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", nullable = false)
    private User createdBy;
}
