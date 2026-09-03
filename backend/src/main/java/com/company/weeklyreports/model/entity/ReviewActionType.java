package com.company.weeklyreports.model.entity;

/**
 * The outcome of a manager's review of a submitted report. Drives the
 * report's status transition (APPROVED, or back to NEEDS_CORRECTION).
 */
public enum ReviewActionType {
    APPROVED,
    REQUESTED_CHANGES
}
