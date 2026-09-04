package com.company.weeklyreports.model.entity;

/**
 * Report lifecycle states (system-design.md §3). Transitions between these are validated by
 * a dedicated state-machine service, not scattered checks — see service/ReportStatusMachine.
 */
public enum ReportStatus {
    DRAFT,
    SUBMITTED,
    NEEDS_CORRECTION,
    APPROVED
}
