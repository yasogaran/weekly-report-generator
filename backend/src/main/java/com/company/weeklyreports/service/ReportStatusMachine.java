package com.company.weeklyreports.service;

import com.company.weeklyreports.exception.ConflictException;
import com.company.weeklyreports.model.entity.ReportStatus;
import org.springframework.stereotype.Component;

/**
 * Every Report status transition is validated here — a dedicated, stateless service
 * (system-design.md §3: "implemented as a dedicated state-machine service — not scattered
 * if checks across controllers"), not spread across ReportServiceImpl/ReviewServiceImpl as
 * inline conditionals. Each method is a pure function of the current status: given the same
 * status, it always makes the same decision, which is what makes this trivially unit-testable
 * in isolation (build-plan.md Block 5) without needing a Report/DB fixture at all.
 * <p>
 * The full machine (system-design.md §3):
 * <pre>
 * DRAFT ──submit──▶ SUBMITTED
 * SUBMITTED ──approve──▶ APPROVED (terminal)
 * SUBMITTED ──request changes──▶ NEEDS_CORRECTION
 * NEEDS_CORRECTION ──edit + resubmit──▶ SUBMITTED (new version — see ReportServiceImpl)
 * </pre>
 */
@Component
public class ReportStatusMachine {

    /** DRAFT and NEEDS_CORRECTION are the only editable statuses — SUBMITTED/APPROVED reject with 409 (api-doc.md, PATCH /reports/{id}). */
    public void validateEditable(ReportStatus current) {
        if (current == ReportStatus.SUBMITTED || current == ReportStatus.APPROVED) {
            throw new ConflictException(
                    "This report can't be edited in its current status (" + current + ").");
        }
    }

    /** Only DRAFT or NEEDS_CORRECTION can be submitted (api-doc.md, POST /reports/{id}/submit). */
    public void validateSubmittable(ReportStatus current) {
        if (current != ReportStatus.DRAFT && current != ReportStatus.NEEDS_CORRECTION) {
            throw new ConflictException(
                    "This report can't be submitted from its current status (" + current + ").");
        }
    }

    /** Only SUBMITTED reports can be reviewed — approving/rejecting anything else 409s (api-doc.md). */
    public void validateReviewable(ReportStatus current) {
        if (current != ReportStatus.SUBMITTED) {
            throw new ConflictException(
                    "This report isn't awaiting review (current status: " + current + ").");
        }
    }
}
