package com.company.weeklyreports.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.security.Key;
import java.util.Date;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Generates and validates JWTs. Claims embedded here (id/name/email/role) are a contract
 * with the frontend: lib/authContext.tsx decodes this same token client-side (without
 * verifying the signature — display only) to restore the session on page load, so the claim
 * names below must stay exactly "id"/"name"/"email"/"role", not e.g. "sub"/"userId".
 */
@Component
public class JwtTokenProvider {

    private final Key signingKey;
    private final long expirationMs;

    public JwtTokenProvider(
            @Value("${app.jwt.secret}") String secret,
            @Value("${app.jwt.expiration-ms}") long expirationMs) {
        // HMAC-SHA key derived from the configured secret — same key used to sign and verify,
        // since this is a single-service backend (no separate token-issuing party to trust).
        this.signingKey = Keys.hmacShaKeyFor(secret.getBytes());
        this.expirationMs = expirationMs;
    }

    public String generateToken(UserPrincipal principal) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + expirationMs);

        return Jwts.builder()
                .subject(principal.getEmail())
                .claim("id", principal.getId())
                .claim("name", principal.getName())
                .claim("email", principal.getEmail())
                .claim("role", principal.getAuthorities().iterator().next().getAuthority().replace("ROLE_", ""))
                .issuedAt(now)
                .expiration(expiry)
                .signWith((SecretKey) signingKey)
                .compact();
    }

    /** Returns the subject (email) if the token's signature and expiry are valid, null otherwise. */
    public String getEmailFromToken(String token) {
        return parseClaims(token).getSubject();
    }

    public boolean isValid(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException ex) {
            return false;
        }
    }

    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith((SecretKey) signingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
