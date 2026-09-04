package com.company.weeklyreports.exception;

import java.time.LocalDateTime;

/**
 * The shape of every error body GlobalExceptionHandler returns. Lives here
 * in exception/ rather than model/dto/ - it isn't a business-domain DTO
 * describing a User/Report/etc., it's purely an artifact of the exception-
 * handling layer, only ever constructed by GlobalExceptionHandler.
 *
 * A record fits naturally: this is an immutable bag of values with no
 * behavior, which is exactly what records are for.
 */
public record ErrorResponse(
        LocalDateTime timestamp,
        int status,
        String error,
        String message,
        String path
) {
}
