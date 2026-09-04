package com.company.weeklyreports.service;

import com.company.weeklyreports.exception.InvalidStateTransitionException;
import com.company.weeklyreports.model.entity.ReportStatus;
import com.company.weeklyreports.model.entity.Role;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Validates Report status transitions against a fixed lookup table rather
 * than the classic GoF State pattern (one class per state, each knowing how
 * to handle every action). With only four statuses and three actions, the
 * full transition table (docs/system-design.md section 3) is four rows -
 * a Map keyed on (status, action, role) says everything a set of State
 * subclasses would, with far less ceremony and no polymorphism to navigate
 * just to answer a single yes/no question. This is a pure validator: it
 * takes no repository, causes no side effects, and knows nothing about how
 * a Report is persisted - it only answers "is this transition legal, and if
 * so, what's the resulting status."
 */
@Component
public class ReportStateMachineService {

    private final Map<TransitionKey, ReportStatus> legalTransitions;

    public ReportStateMachineService() {
        this.legalTransitions = Map.of(
                new TransitionKey(ReportStatus.DRAFT, ReportTransitionAction.SUBMIT, Role.TEAM_MEMBER), ReportStatus.SUBMITTED,
                new TransitionKey(ReportStatus.NEEDS_CORRECTION, ReportTransitionAction.SUBMIT, Role.TEAM_MEMBER), ReportStatus.SUBMITTED,
                new TransitionKey(ReportStatus.SUBMITTED, ReportTransitionAction.APPROVE, Role.MANAGER), ReportStatus.APPROVED,
                new TransitionKey(ReportStatus.SUBMITTED, ReportTransitionAction.REQUEST_CHANGES, Role.MANAGER), ReportStatus.NEEDS_CORRECTION
        );
    }

    // Looks up (currentStatus, action, actorRole) in the transition table.
    // A single missing-key check covers all three failure modes at once -
    // wrong status, wrong role, or a nonsensical action - since any
    // combination not explicitly listed as legal is rejected by default.
    public ReportStatus transition(ReportStatus currentStatus, ReportTransitionAction action, Role actorRole) {
        TransitionKey key = new TransitionKey(currentStatus, action, actorRole);
        ReportStatus nextStatus = legalTransitions.get(key);
        if (nextStatus == null) {
            throw new InvalidStateTransitionException(currentStatus, action, actorRole);
        }
        return nextStatus;
    }

    // Composite lookup key. A record is a natural fit here - it's an
    // immutable value with structural equals()/hashCode() generated for
    // free, which is exactly what's needed to use it as a Map key.
    private record TransitionKey(ReportStatus status, ReportTransitionAction action, Role role) {
    }
}
