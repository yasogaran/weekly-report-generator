package com.company.weeklyreports.exception;

/**
 * Thrown when a requested resource doesn't exist - or, for ownership-scoped
 * lookups (e.g. a team member fetching a report by id), when it exists but
 * doesn't belong to the caller. Both cases use this same exception and
 * message shape on purpose: a team member probing another member's report
 * id must see an identical "not found" response either way, never a
 * different error that would confirm the id belongs to someone else.
 */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }
}
