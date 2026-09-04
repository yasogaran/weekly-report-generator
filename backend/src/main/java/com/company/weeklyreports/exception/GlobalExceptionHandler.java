package com.company.weeklyreports.exception;

import com.company.weeklyreports.model.dto.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

/**
 * Centralizes every exception -> HTTP response mapping in one place, so controllers never
 * need their own try/catch (CLAUDE.md's "centralized exception handling" pattern). Every
 * handler below builds the exact same ErrorResponse shape the frontend's apiClient.ts
 * expects (docs/api/api-doc.md), just with a different status/message per exception type.
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    private ErrorResponse build(HttpStatus status, String message, HttpServletRequest request) {
        return ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(status.value())
                .error(status.getReasonPhrase())
                .message(message)
                .path(request.getRequestURI())
                .build();
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(ResourceNotFoundException ex, HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(build(HttpStatus.NOT_FOUND, ex.getMessage(), request));
    }

    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ErrorResponse> handleValidation(ValidationException ex, HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(build(HttpStatus.BAD_REQUEST, ex.getMessage(), request));
    }

    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<ErrorResponse> handleConflict(ConflictException ex, HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(build(HttpStatus.CONFLICT, ex.getMessage(), request));
    }

    /** @Valid failures on request DTOs (e.g. @NotBlank, @Email) — collapsed into one readable message. */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleBeanValidation(
            MethodArgumentNotValidException ex, HttpServletRequest request) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + " " + error.getDefaultMessage())
                .collect(Collectors.joining("; "));
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(build(HttpStatus.BAD_REQUEST, message, request));
    }

    /** Malformed JSON in the request body — same 400 bucket as bean-validation failures (api-doc.md). */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleMalformedJson(
            HttpMessageNotReadableException ex, HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(build(HttpStatus.BAD_REQUEST, "Malformed request body.", request));
    }

    /** Bad login credentials — deliberately the same generic message regardless of which part was wrong. */
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleBadCredentials(HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(build(HttpStatus.UNAUTHORIZED, "Invalid email or password.", request));
    }

    /** A deactivated account trying to log in — same 401 bucket as bad credentials (api-doc.md). */
    @ExceptionHandler(DisabledException.class)
    public ResponseEntity<ErrorResponse> handleDisabledAccount(HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(build(HttpStatus.UNAUTHORIZED, "Invalid email or password.", request));
    }

    /** @PreAuthorize role-check failures. */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDenied(AccessDeniedException ex, HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(build(HttpStatus.FORBIDDEN, "You don't have permission to do that.", request));
    }

    /**
     * Spring's own "nothing matched this URL" exception — thrown for any request that isn't
     * one of /auth/register, /auth/login, or an endpoint a controller actually implements.
     * Without this specific handler, the catch-all Exception handler below would swallow it
     * and turn a legitimate 404 (unmapped route) into a misleading 500 — exactly what was
     * happening for every /api/* endpoint not yet built (projects, users, dashboard, reports).
     */
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErrorResponse> handleNoRouteMatched(HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(build(HttpStatus.NOT_FOUND, "No such endpoint.", request));
    }

    /** Last resort — logs the real exception server-side, returns a generic message to the client (api-doc.md). */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnexpected(Exception ex, HttpServletRequest request) {
        log.error("Unhandled exception on {} {}", request.getMethod(), request.getRequestURI(), ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(build(HttpStatus.INTERNAL_SERVER_ERROR, "Something went wrong. Please try again.", request));
    }
}
