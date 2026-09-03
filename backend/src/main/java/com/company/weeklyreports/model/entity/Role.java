package com.company.weeklyreports.model.entity;

/**
 * The two roles supported by the system. Kept deliberately small (no separate
 * Admin role) per the RBAC spec - see docs/rbac-matrix.md.
 */
public enum Role {
    TEAM_MEMBER,
    MANAGER
}
