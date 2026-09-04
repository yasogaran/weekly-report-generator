package com.company.weeklyreports.security;

import com.company.weeklyreports.model.entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * Signs and verifies the JWTs that carry authentication between requests in
 * this stateless API. The token's subject is the user's id (the stable,
 * never-reused identifier); email and role are carried as claims purely
 * for convenience/debugging - authorization itself never trusts the role
 * claim (see JwtAuthFilter, which re-loads the user's current role from the
 * database via CustomUserDetailsService on every request), since a token
 * issued before a role change would otherwise keep granting the old role
 * until it expires.
 *
 * Reads app.jwt.secret/app.jwt.expiration-ms via @Value rather than
 * @ConfigurationProperties: with only two flat scalar values, a dedicated
 * properties class would be a whole extra file for no real benefit.
 */
@Component
public class JwtTokenProvider {

    private final SecretKey key;
    private final long expirationMs;

    public JwtTokenProvider(@Value("${app.jwt.secret}") String secret,
                             @Value("${app.jwt.expiration-ms}") long expirationMs) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expirationMs = expirationMs;
    }

    // Issues a new token for a just-authenticated (or just-registered) user.
    public String generateToken(User user) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + expirationMs);
        return Jwts.builder()
                .subject(String.valueOf(user.getId()))
                .claim("email", user.getEmail())
                .claim("role", user.getRole().name())
                .issuedAt(now)
                .expiration(expiry)
                .signWith(key)
                .compact();
    }

    // True if the token's signature and expiry both check out. Any parse
    // failure (bad signature, malformed token, expired) is treated as
    // simply "not valid" rather than propagated - JwtAuthFilter only needs
    // a yes/no answer.
    public boolean validateToken(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    public Long getUserIdFromToken(String token) {
        return Long.valueOf(parseClaims(token).getSubject());
    }

    // Used by JwtAuthFilter to look the caller up via
    // CustomUserDetailsService, which is keyed by email (our login
    // identifier), not id.
    public String getEmailFromToken(String token) {
        return parseClaims(token).get("email", String.class);
    }

    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
