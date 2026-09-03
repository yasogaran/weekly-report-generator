package com.company.weeklyreports.model.entity;

/**
 * The report lifecycle states. Transitions between these are validated by a
 * dedicated state-machine service (not here) - see docs/system-design.md
 * section 3 for the allowed transition graph:
 * DRAFT -> SUBMITTED -> APPROVED, or SUBMITTED -> NEEDS_CORRECTION -> SUBMITTED (new version).
 */
public enum ReportStatus {
    DRAFT,
    SUBMITTED,
    NEEDS_CORRECTION,
    APPROVED
}
