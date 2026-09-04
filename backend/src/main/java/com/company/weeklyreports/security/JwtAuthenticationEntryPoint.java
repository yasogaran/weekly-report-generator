package com.company.weeklyreports.security;

import com.company.weeklyreports.exception.ErrorResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;

/**
 * Produces the 401 response for a request that never authenticated at all
 * (no Authorization header, or a token JwtAuthFilter couldn't validate -
 * missing, malformed, or expired). This runs BEFORE the request ever
 * reaches a controller, from Spring Security's ExceptionTranslationFilter -
 * that's the whole reason it has to exist as its own class: GlobalExceptionHandler's
 * @RestControllerAdvice only intercepts exceptions thrown during
 * DispatcherServlet's handler dispatch, never ones thrown by a security
 * filter upstream of it. Without this class, Spring Security falls back to
 * its default Http403ForbiddenEntryPoint, which is exactly the 401-vs-403
 * conflation this class fixes - see SecurityConfig for where it's wired in.
 *
 * Reuses ErrorResponse (the same shape GlobalExceptionHandler's handlers
 * return) rather than inventing a second error body shape, so a client
 * never has to branch on "was this a controller-thrown error or a
 * filter-thrown one" - the JSON looks identical either way.
 */
@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    public JwtAuthenticationEntryPoint(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    // Deliberately generic message: missing header, malformed token, and
    // expired token are all reported identically ("Authentication
    // required") rather than distinguishing which one occurred. Echoing
    // back "token expired" vs "token malformed" vs "no token" would hand
    // an attacker a free signal about why their attempt failed - the same
    // no-information-leak principle already applied to
    // ResourceNotFoundException's identical treatment of "doesn't exist"
    // vs "exists but isn't yours."
    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                          AuthenticationException authException) throws IOException {
        ErrorResponse errorResponse = new ErrorResponse(
                LocalDateTime.now(),
                HttpStatus.UNAUTHORIZED.value(),
                HttpStatus.UNAUTHORIZED.getReasonPhrase(),
                "Authentication required",
                request.getRequestURI());

        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
    }
}
