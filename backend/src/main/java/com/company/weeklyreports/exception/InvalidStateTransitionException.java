package com.company.weeklyreports.exception;

import com.company.weeklyreports.model.entity.ReportStatus;
import com.company.weeklyreports.model.entity.Role;
import com.company.weeklyreports.service.ReportTransitionAction;

/**
 * Thrown by ReportStateMachineService when a requested (status, action, role)
 * combination isn't a legal Report transition. Carries the actual status and
 * action in the message so a stack trace alone is enough to see what was
 * attempted, without digging through logs for the request payload.
 */
public class InvalidStateTransitionException extends RuntimeException {

    public InvalidStateTransitionException(ReportStatus currentStatus, ReportTransitionAction action, Role actorRole) {
        super(String.format(
                "Cannot %s a report in %s status as %s",
                action, currentStatus, actorRole));
    }
}
