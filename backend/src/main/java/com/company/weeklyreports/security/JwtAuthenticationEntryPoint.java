package com.company.weeklyreports.security;

import com.company.weeklyreports.model.dto.ErrorResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

/**
 * Handles requests that never reach a controller at all — no/invalid/expired JWT on a
 * protected endpoint. This is a filter-chain-level rejection (Spring Security's job, not
 * Spring MVC's), so GlobalExceptionHandler's @ControllerAdvice can't catch it; this writes
 * the exact same ErrorResponse JSON shape directly so the frontend's apiClient.ts still
 * parses a consistent error body either way.
 */
@Component
@RequiredArgsConstructor
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    @Override
    public void commence(
            HttpServletRequest request, HttpServletResponse response, AuthenticationException authException)
            throws IOException, ServletException {
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        ErrorResponse body = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.UNAUTHORIZED.value())
                .error(HttpStatus.UNAUTHORIZED.getReasonPhrase())
                .message("Missing or invalid authentication token.")
                .path(request.getRequestURI())
                .build();

        objectMapper.writeValue(response.getWriter(), body);
    }
}
