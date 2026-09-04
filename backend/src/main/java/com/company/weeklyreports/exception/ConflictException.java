package com.company.weeklyreports.exception;

/**
 * Maps to 409. For requests that are well-formed but conflict with the resource's current
 * state — e.g. approving a report that isn't SUBMITTED (api-doc.md). Not in system-design.md
 * §5a's original exception list, but the state machine (Block 5) can't produce a 409 without
 * something to throw for it.
 */
public class ConflictException extends RuntimeException {
    public ConflictException(String message) {
        super(message);
    }
}
