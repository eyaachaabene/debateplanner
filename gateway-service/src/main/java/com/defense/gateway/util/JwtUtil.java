package com.defense.gateway.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.stream.Collectors;

/**
 * Stateless JWT utility — validates tokens and extracts claims locally.
 * Never calls the auth-service; the shared secret enables self-contained validation.
 */
@Component
public class JwtUtil {

    private final SecretKey signingKey;

    public JwtUtil(@Value("${jwt.secret-key}") String secret) {
        this.signingKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Parse and fully validate the token (signature + expiration).
     *
     * @param token raw JWT string (without "Bearer " prefix)
     * @return parsed Claims
     * @throws JwtException if the token is invalid or expired
     */
    public Claims validateAndExtract(String token) {
        return Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * Extract username from JWT subject.
     * In auth-service, the username is stored in the standard "sub" claim.
     */
    public String getUsername(Claims claims) {
        return claims.getSubject() != null ? claims.getSubject() : "";
    }

    /**
     * Extract roles from claims.
     * Supports:
     * - a collection claim: roles = ["ROLE_ADMIN", "ROLE_STUDENT"]
     * - or a plain string claim: roles = "ROLE_ADMIN"
     *
     * Returns a comma-separated string suitable for X-User-Roles.
     */
    public String getRoles(Claims claims) {
        Object roles = claims.get("roles");

        if (roles == null) {
            return "";
        }

        if (roles instanceof Collection<?> collection) {
            return collection.stream()
                    .map(String::valueOf)
                    .collect(Collectors.joining(","));
        }

        return roles.toString();
    }
}