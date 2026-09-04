package com.company.weeklyreports.exception;

/**
 * Maps to 400. For business-rule validation failures that aren't expressible as a
 * bean-validation annotation on a request DTO — e.g. "comment is required when requesting
 * changes" (api-doc.md), which depends on another field's value, not just this field alone.
 */
public class ValidationException extends RuntimeException {
    public ValidationException(String message) {
        super(message);
    }
}
