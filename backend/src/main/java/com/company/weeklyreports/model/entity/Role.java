package com.company.weeklyreports.model.entity;

/**
 * The only two roles this app has (rbac-matrix.md explicitly calls out: no separate Admin
 * role — don't over-engineer the permission model beyond what the spec asks for).
 */
public enum Role {
    TEAM_MEMBER,
    MANAGER
}
