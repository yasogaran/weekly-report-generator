package com.company.weeklyreports.exception;

/**
 * Maps to 404. Deliberately reused for both "this row truly doesn't exist" and "it exists
 * but you don't own it" (api-doc.md RBAC note: the two are indistinguishable on purpose, to
 * stop ID enumeration from ever confirming a report/user id is real).
 */
public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
}
