package com.company.weeklyreports.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

/**
 * Central place where every exception that escapes a controller gets
 * turned into a consistent JSON error body and the right HTTP status -
 * no controller method needs its own try/catch. @RestControllerAdvice is
 * @ControllerAdvice + @ResponseBody combined, so each handler method below
 * can just return an ErrorResponse directly and have it serialized as the
 * response body.
 *
 * Note on method ordering: Spring resolves @ExceptionHandler methods by
 * exception type hierarchy (ExceptionHandlerMethodResolver picks the
 * closest matching declared type for the thrown exception), not by the
 * order these methods are declared in the class. The catch-all
 * Exception handler at the bottom is placed last purely for readability -
 * it would behave identically declared first, since every other handler
 * here targets a more specific type and will always be preferred over it.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    // 404: the report/project/etc. either doesn't exist, or - for
    // ownership-scoped lookups - exists but isn't the caller's. Both cases
    // intentionally produce this same exception/status/message shape so a
    // team member probing another member's id can't tell the difference.
    @ExceptionHandler(ResourceNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleResourceNotFound(ResourceNotFoundException ex, HttpServletRequest request) {
        return build(HttpStatus.NOT_FOUND, ex.getMessage(), request);
    }

    // 400: a business-rule validation failure that isn't expressible as a
    // static bean-validation annotation (e.g. a comment required only
    // when a review action is REQUESTED_CHANGES, or a duplicate email at
    // registration) - same category as a malformed request.
    @ExceptionHandler(ValidationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleValidation(ValidationException ex, HttpServletRequest request) {
        return build(HttpStatus.BAD_REQUEST, ex.getMessage(), request);
    }

    // 409: the request is well-formed but conflicts with the report's
    // current server-side state (e.g. APPROVE on a DRAFT report) - not a
    // malformed-input problem, so 400 doesn't fit; 409 is HTTP's status
    // for "valid request, but it conflicts with the resource's current
    // state," which is exactly what a rejected FSM transition is.
    @ExceptionHandler(InvalidStateTransitionException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorResponse handleInvalidStateTransition(InvalidStateTransitionException ex, HttpServletRequest request) {
        return build(HttpStatus.CONFLICT, ex.getMessage(), request);
    }

    // 409: discovered as a gap while writing accurate Swagger docs for
    // ReportController.updateReport - this exception had no handler at
    // all before now and was silently falling through to the generic 500
    // below despite being just as much a "valid request, wrong resource
    // state" case as InvalidStateTransitionException (editing a
    // SUBMITTED/APPROVED report is well-formed, it's just not allowed
    // right now). Same 409 reasoning applies.
    @ExceptionHandler(ReportNotEditableException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorResponse handleReportNotEditable(ReportNotEditableException ex, HttpServletRequest request) {
        return build(HttpStatus.CONFLICT, ex.getMessage(), request);
    }

    // 400: @Valid failed on a request DTO (e.g. @NotBlank/@Email on
    // RegisterRequest). Spring's default MethodArgumentNotValidException
    // message is a verbose object dump; this collapses it into one
    // readable "field: reason" line per failing field instead.
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleBeanValidation(MethodArgumentNotValidException ex, HttpServletRequest request) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(fieldError -> fieldError.getField() + ": " + fieldError.getDefaultMessage())
                .collect(Collectors.joining("; "));
        return build(HttpStatus.BAD_REQUEST, message, request);
    }

    // 400: the request body failed to deserialize at all - most notably,
    // an enum field (e.g. UpdateUserRoleRequest.role) given a string that
    // isn't one of its declared constants. This never reaches bean
    // validation (@NotNull can't run on a field Jackson couldn't populate
    // in the first place), so without this handler it would fall through
    // to the generic 500 below despite being a plain client input error.
    // Added alongside the User Management task's role-update endpoint,
    // which is what surfaced this gap.
    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleMalformedRequestBody(HttpMessageNotReadableException ex, HttpServletRequest request) {
        return build(HttpStatus.BAD_REQUEST, "Malformed or invalid request body", request);
    }

    // 403: Spring Security's own exception, thrown when an authenticated
    // user's role fails a @PreAuthorize check (e.g. a TEAM_MEMBER hitting
    // POST /api/reports/{id}/reviews). The caller is known and
    // authenticated - they're just not allowed to do this - which is
    // exactly the 401 vs 403 distinction: 401 means "who are you," 403
    // means "I know who you are, and the answer is no."
    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ErrorResponse handleAccessDenied(AccessDeniedException ex, HttpServletRequest request) {
        return build(HttpStatus.FORBIDDEN, "You do not have permission to perform this action", request);
    }

    // 401: thrown out of AuthenticationManager.authenticate() in
    // AuthService.login() when credentials don't check out.
    // BadCredentialsException is a subclass of AuthenticationException, so
    // it's covered here too without a dedicated handler - Spring's
    // hierarchy-based matching (see class-level comment) picks this
    // handler for any AuthenticationException subtype.
    @ExceptionHandler(AuthenticationException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ErrorResponse handleAuthentication(AuthenticationException ex, HttpServletRequest request) {
        return build(HttpStatus.UNAUTHORIZED, "Invalid email or password", request);
    }

    // 500: anything not already handled above. The full exception (with
    // stack trace) is logged server-side for debugging, but the response
    // body stays generic on purpose - an unexpected exception's message
    // could contain internal details (SQL, file paths, class names) that
    // have no business leaving the server.
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponse handleUnexpected(Exception ex, HttpServletRequest request) {
        log.error("Unhandled exception while processing {} {}", request.getMethod(), request.getRequestURI(), ex);
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred", request);
    }

    private ErrorResponse build(HttpStatus status, String message, HttpServletRequest request) {
        return new ErrorResponse(
                LocalDateTime.now(),
                status.value(),
                status.getReasonPhrase(),
                message,
                request.getRequestURI());
    }
}
