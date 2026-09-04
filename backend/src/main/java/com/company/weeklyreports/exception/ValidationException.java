package com.company.weeklyreports.exception;

/**
 * Thrown for business-rule validation failures that can't be expressed as
 * a static bean-validation annotation on a DTO - typically because the
 * rule is conditional on another field's value (e.g. a comment being
 * required only when a review action is REQUESTED_CHANGES).
 */
public class ValidationException extends RuntimeException {

    public ValidationException(String message) {
        super(message);
    }
}
