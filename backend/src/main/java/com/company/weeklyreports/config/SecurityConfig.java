package com.company.weeklyreports.config;

import com.company.weeklyreports.security.JwtAuthFilter;
import com.company.weeklyreports.security.JwtAuthenticationEntryPoint;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

/**
 * Wires together the security building blocks: which endpoints require
 * authentication, how passwords are hashed, how login credentials are
 * verified, and where JwtAuthFilter sits in the filter chain.
 * @EnableMethodSecurity is what makes @PreAuthorize on controller methods
 * (ReportController, ReviewController) actually take effect.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    // Request flow, in order, once this chain is registered:
    //
    // 0. For a cross-origin browser request, the CORS check (wired via
    //    .cors(...) below) runs first: for a preflight OPTIONS request,
    //    Spring's CorsFilter answers it directly using the
    //    CorsConfigurationSource bean and short-circuits the rest of the
    //    chain; for a real request, it validates the Origin header and
    //    adds the Access-Control-Allow-* response headers. IMPORTANT: CORS
    //    is a browser-enforced restriction, not a server-side auth
    //    mechanism - it stops a browser from letting JavaScript on another
    //    origin read the response, but it does nothing to stop a non-browser
    //    client (curl, Postman, another server) from calling this API
    //    directly with no Origin header at all. It is an orthogonal
    //    concern to authentication/authorization: it never replaces, and
    //    is never a substitute for, JwtAuthFilter/@PreAuthorize below - an
    //    unauthenticated or under-privileged request is still rejected
    //    exactly as before regardless of what Origin it claims.
    // 1. Every incoming request then passes through JwtAuthFilter
    //    (inserted below via addFilterBefore). If it carries a valid
    //    Bearer token, JwtAuthFilter populates SecurityContextHolder with
    //    an Authentication for that user; if the token is missing or
    //    invalid, it does nothing and the context stays empty (anonymous).
    // 2. The rest of Spring Security's standard filters run next,
    //    including UsernamePasswordAuthenticationFilter - this app never
    //    actually uses that filter (there's no HTML form login), it's only
    //    referenced here as the fixed point JwtAuthFilter is inserted
    //    before.
    // 3. authorizeHttpRequests then decides, per request path, whether the
    //    (possibly still-empty) SecurityContext is sufficient:
    //    /api/auth/** is permitAll (register/login must work with no
    //    token yet), everything else requires authenticated(). A request
    //    that fails this check never reaches a controller -
    //    ExceptionTranslationFilter turns the failure into a 401
    //    automatically; no controller code has to produce that response.
    // 4. Only once a request clears step 3 does it reach the
    //    DispatcherServlet and the matching @RestController method -
    //    where @PreAuthorize (enabled by @EnableMethodSecurity above)
    //    applies a second, finer-grained role check via AOP immediately
    //    before the method body runs. A failure HERE throws Spring
    //    Security's AccessDeniedException, which GlobalExceptionHandler
    //    catches and maps to 403 - this is the "authenticated, but wrong
    //    role" case, and is completely unaffected by anything below.
    //
    // What happens when step 3 itself REJECTS a request (no Authentication
    // at all, or one JwtAuthFilter couldn't build from an invalid/expired
    // token): Spring Security throws an AuthenticationException, which
    // never reaches GlobalExceptionHandler (that only sees exceptions from
    // inside the DispatcherServlet dispatch, and this request never gets
    // that far). Instead ExceptionTranslationFilter hands it to whatever
    // AuthenticationEntryPoint is configured - by default,
    // Http403ForbiddenEntryPoint, which is exactly the "401 comes back as
    // 403" bug this fixes. Wiring .exceptionHandling(...) below to
    // JwtAuthenticationEntryPoint makes this case correctly return 401
    // instead, without touching the @PreAuthorize/403 path at all - the
    // two failure modes are handled by two entirely different Spring
    // Security mechanisms (AuthenticationEntryPoint vs.
    // AccessDeniedHandler/@RestControllerAdvice), so fixing one cannot
    // accidentally affect the other.
    //
    // CSRF is disabled and sessions are STATELESS because this is a
    // token-based API: there's no server-side session and no cookie-based
    // auth for CSRF to protect in the first place.
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http,
                                                     JwtAuthFilter jwtAuthFilter,
                                                     JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint,
                                                     CorsConfigurationSource corsConfigurationSource) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource))
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(ex -> ex.authenticationEntryPoint(jwtAuthenticationEntryPoint))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/auth/**").permitAll()
                        // Swagger's own UI/spec endpoints - permitAll here
                        // only makes the DOCUMENTATION viewable without a
                        // token. It has no bearing on the actual endpoints
                        // those docs describe: every /api/reports/**,
                        // /api/dashboard/**, etc. request still falls
                        // through to .anyRequest().authenticated() below,
                        // and every @PreAuthorize check still runs exactly
                        // as before. See the explanation delivered
                        // alongside this task for why that separation holds.
                        .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
                        .anyRequest().authenticated())
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    // Defines which cross-origin browser requests are allowed to reach
    // this API at all - registered on the filter chain above via .cors(...)
    // rather than as a second, separate filter chain, so there's exactly
    // one place that decides both "is this origin allowed" and "is this
    // request authenticated/authorized." The origin is read from a
    // property (app.cors.allowed-origin) rather than hardcoded, so
    // pointing this at a deployed frontend later is a config change, not
    // a code change.
    @Bean
    public CorsConfigurationSource corsConfigurationSource(
            @Value("${app.cors.allowed-origin}") String allowedOrigin) {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of(allowedOrigin));
        configuration.setAllowedMethods(List.of("GET", "POST", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("Authorization", "Content-Type"));
        // Harmless if the frontend never sends cookies (it currently
        // doesn't - auth is a Bearer token in a header), but required if
        // that ever changes.
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    // Used both to hash new passwords on registration and, transparently,
    // by the auto-configured DaoAuthenticationProvider to verify a login
    // attempt's raw password against the stored hash.
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // Exposes the AuthenticationManager Spring Boot already assembles
    // (wired to CustomUserDetailsService + the PasswordEncoder bean above,
    // via an auto-configured DaoAuthenticationProvider) so AuthService can
    // call .authenticate() during login.
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}
