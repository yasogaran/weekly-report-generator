package com.company.weeklyreports.model.dto;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

/**
 * The exact error shape documented in docs/api/api-doc.md — every error response returned
 * by GlobalExceptionHandler has this shape, so the frontend can always parse a failed
 * response the same way regardless of which endpoint or exception produced it.
 */
@Getter
@AllArgsConstructor
@Builder
public class ErrorResponse {
    private LocalDateTime timestamp;
    private int status;
    private String error;
    private String message;
    private String path;
}
