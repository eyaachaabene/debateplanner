package com.defense.gateway.filter;

import com.defense.gateway.util.JwtUtil;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for the JWT authentication filter.
 *
 * <p>Uses WebTestClient against a running application context.
 * Routes are not resolvable in unit-test context so we assert HTTP status codes
 * returned by the gateway itself (401 before routing, etc.).
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = {
        "jwt.secret=TestSecretKeyForUnitTestingThatIsAtLeast256BitsLongOK!",
        "spring.cloud.gateway.routes[0].id=test",
        "spring.cloud.gateway.routes[0].uri=http://localhost:9999",
        "spring.cloud.gateway.routes[0].predicates[0]=Path=/api/students/**"
})
class JwtAuthenticationFilterTest {

    private static final String TEST_SECRET =
            "TestSecretKeyForUnitTestingThatIsAtLeast256BitsLongOK!";

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private JwtUtil jwtUtil;

    private SecretKey signingKey;

    @BeforeEach
    void setUp() {
        signingKey = Keys.hmacShaKeyFor(TEST_SECRET.getBytes(StandardCharsets.UTF_8));
    }

    // ── Helper: mint a token ──────────────────────────────────────────────────

    private String mintToken(String userId, String role, Date expiration) {
        return Jwts.builder()
                .subject("testuser@example.com")
                .claim("userId", userId)
                .claim("role", role)
                .expiration(expiration)
                .signWith(signingKey)
                .compact();
    }

    // ── Tests ─────────────────────────────────────────────────────────────────

    @Test
    void publicRoute_noToken_shouldBeForwarded() {
        // /api/auth/login is public — gateway should not return 401
        // (it may return 502 if auth-service is unreachable in test env)
        webTestClient.post()
                .uri("/api/auth/login")
                .exchange()
                .expectStatus().isNotEqualTo(401);
    }

    @Test
    void protectedRoute_noToken_shouldReturn401() {
        webTestClient.get()
                .uri("/api/students")
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    void protectedRoute_invalidToken_shouldReturn401() {
        webTestClient.get()
                .uri("/api/students")
                .header("Authorization", "Bearer this.is.not.valid")
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    void protectedRoute_expiredToken_shouldReturn401() {
        Date expired = new Date(System.currentTimeMillis() - 60_000);
        String token = mintToken("user-1", "STUDENT", expired);

        webTestClient.get()
                .uri("/api/students")
                .header("Authorization", "Bearer " + token)
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    void jwtUtil_validToken_extractsClaimsCorrectly() {
        Date expiration = new Date(System.currentTimeMillis() + 60_000);
        String token = mintToken("42", "PROFESSOR", expiration);

        Claims claims = jwtUtil.validateAndExtract(token);

        assertThat(jwtUtil.getUserId(claims)).isEqualTo("42");
        assertThat(jwtUtil.getRole(claims)).isEqualTo("PROFESSOR");
    }

    @Test
    void jwtUtil_missingUserId_fallsBackToSubject() {
        // Token without "userId" claim — should fall back to subject
        String token = Jwts.builder()
                .subject("fallback@example.com")
                .claim("role", "ADMIN")
                .expiration(new Date(System.currentTimeMillis() + 60_000))
                .signWith(signingKey)
                .compact();

        Claims claims = jwtUtil.validateAndExtract(token);
        assertThat(jwtUtil.getUserId(claims)).isEqualTo("fallback@example.com");
    }
}
