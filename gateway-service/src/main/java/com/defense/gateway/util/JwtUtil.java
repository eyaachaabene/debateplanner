package com.defense.gateway.util;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;

/**
 * Stateless JWT utility — validates tokens and extracts claims locally.
 * Never calls the auth-service; the shared secret enables self-contained validation.
 */
@Slf4j
@Component
public class JwtUtil {

    private final SecretKey signingKey;

    public JwtUtil(@Value("${jwt.secret}") String secret) {
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
     * Extract userId from claims. Falls back to subject if "userId" claim is absent.
     * Matches the claim name used by auth-service when signing the token.
     */
    public String getUserId(Claims claims) {
        Object userId = claims.get("userId");
        return userId != null ? userId.toString() : claims.getSubject();
    }

    /**
     * Extract role from claims. Auth-service embeds "role" as a plain string claim.
     */
    public String getRole(Claims claims) {
        Object role = claims.get("role");
        return role != null ? role.toString() : "";
    }
}
