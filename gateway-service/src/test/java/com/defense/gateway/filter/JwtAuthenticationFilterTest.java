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
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for the JWT authentication filter.
 *
 * Uses WebTestClient against a running application context.
 * Routes are not resolvable in unit-test context, so we mainly assert
 * the gateway's own behavior (401 before routing, non-401 on public paths, etc.).
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = {
        "jwt.secret-key=TestSecretKeyForUnitTestingThatIsAtLeast256BitsLongOK!",
        "spring.cloud.gateway.routes[0].id=test-students-route",
        "spring.cloud.gateway.routes[0].uri=http://localhost:9999",
        "spring.cloud.gateway.routes[0].predicates[0]=Path=/api/v1/students/**",
        "spring.cloud.gateway.routes[1].id=test-auth-route",
        "spring.cloud.gateway.routes[1].uri=http://localhost:9998",
        "spring.cloud.gateway.routes[1].predicates[0]=Path=/api/v1/auth/**"
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

    // Helper: mint a token matching the current auth-service format
    private String mintToken(String username, List<String> roles, Date expiration) {
        return Jwts.builder()
                .subject(username)
                .claim("roles", roles)
                .expiration(expiration)
                .signWith(signingKey)
                .compact();
    }

    @Test
    void publicRoute_noToken_shouldNotReturn401() {
        // Public route: filter should bypass JWT validation.
        // Downstream route is fake, so response may be 5xx, but must not be 401.
        webTestClient.post()
                .uri("/api/v1/auth/login")
                .exchange()
                .expectStatus()
                .value(status -> assertThat(status).isNotEqualTo(401));
    }

    @Test
    void protectedRoute_noToken_shouldReturn401() {
        webTestClient.get()
                .uri("/api/v1/students/test")
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    void protectedRoute_invalidToken_shouldReturn401() {
        webTestClient.get()
                .uri("/api/v1/students/test")
                .header("Authorization", "Bearer this.is.not.valid")
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    void protectedRoute_expiredToken_shouldReturn401() {
        Date expired = new Date(System.currentTimeMillis() - 60_000);
        String token = mintToken("expireduser@example.com", List.of("ROLE_STUDENT"), expired);

        webTestClient.get()
                .uri("/api/v1/students/test")
                .header("Authorization", "Bearer " + token)
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    void jwtUtil_validToken_extractsClaimsCorrectly() {
        Date expiration = new Date(System.currentTimeMillis() + 60_000);
        String token = mintToken(
                "prof@example.com",
                List.of("ROLE_PROFESSOR"),
                expiration
        );

        Claims claims = jwtUtil.validateAndExtract(token);

        assertThat(jwtUtil.getUsername(claims)).isEqualTo("prof@example.com");
        assertThat(jwtUtil.getRoles(claims)).isEqualTo("ROLE_PROFESSOR");
    }

    @Test
    void jwtUtil_multipleRoles_extractsCommaSeparatedRoles() {
        Date expiration = new Date(System.currentTimeMillis() + 60_000);
        String token = mintToken(
                "admin@example.com",
                List.of("ROLE_ADMIN", "ROLE_PROFESSOR"),
                expiration
        );

        Claims claims = jwtUtil.validateAndExtract(token);

        assertThat(jwtUtil.getUsername(claims)).isEqualTo("admin@example.com");
        assertThat(jwtUtil.getRoles(claims)).isEqualTo("ROLE_ADMIN,ROLE_PROFESSOR");
    }

    @Test
    void jwtUtil_missingRoles_returnsEmptyString() {
        String token = Jwts.builder()
                .subject("fallback@example.com")
                .expiration(new Date(System.currentTimeMillis() + 60_000))
                .signWith(signingKey)
                .compact();

        Claims claims = jwtUtil.validateAndExtract(token);

        assertThat(jwtUtil.getUsername(claims)).isEqualTo("fallback@example.com");
        assertThat(jwtUtil.getRoles(claims)).isEmpty();
    }
}