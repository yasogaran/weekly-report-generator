package com.company.weeklyreports.exception;

import com.company.weeklyreports.model.entity.ReportStatus;

/**
 * Thrown when an edit is attempted on a report whose status doesn't allow
 * edits (SUBMITTED or APPROVED) - per rbac-matrix.md, a member's own report
 * is only editable in DRAFT or NEEDS_CORRECTION.
 */
public class ReportNotEditableException extends RuntimeException {

    public ReportNotEditableException(ReportStatus currentStatus) {
        super("Report cannot be edited while in " + currentStatus + " status");
    }
}
