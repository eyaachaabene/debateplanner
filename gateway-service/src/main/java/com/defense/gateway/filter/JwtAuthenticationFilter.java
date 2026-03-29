package com.defense.gateway.filter;

import com.defense.gateway.util.JwtUtil;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Global JWT authentication filter.
 *
 * <p>Runs on every inbound request. For public routes the request is forwarded
 * immediately. For protected routes the filter:
 * <ol>
 *   <li>Extracts the Bearer token from the Authorization header</li>
 *   <li>Validates the JWT signature and expiration <em>locally</em> (no auth-service call)</li>
 *   <li>Attaches X-User-Id and X-User-Role headers for downstream services</li>
 *   <li>Attaches X-Request-Id for distributed tracing</li>
 *   <li>Returns 401 / 403 on any security violation</li>
 * </ol>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter implements GlobalFilter, Ordered {

    private static final String BEARER_PREFIX = "Bearer ";
    private static final String HEADER_USER_ID  = "X-User-Id";
    private static final String HEADER_USER_ROLE = "X-User-Role";

    private static final List<String> PUBLIC_PATHS = List.of(
            "/api/auth/login",
            "/api/auth/register",
            "/actuator/**"
    );

    private final JwtUtil jwtUtil;
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    @Override
    public int getOrder() {
        // Run before routing filters
        return -100;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();
        String requestId = java.util.UUID.randomUUID().toString();

        log.info("[{}] {} {}", requestId, request.getMethod(), path);

        // ── Public routes: bypass JWT check ──────────────────────────────────
        if (isPublicPath(path)) {
            return chain.filter(addRequestId(exchange, requestId));
        }

        // ── Protected routes: validate JWT ───────────────────────────────────
        String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (authHeader == null || !authHeader.startsWith(BEARER_PREFIX)) {
            log.warn("[{}] Missing or malformed Authorization header", requestId);
            return unauthorizedResponse(exchange, "Missing or malformed Authorization header");
        }

        String token = authHeader.substring(BEARER_PREFIX.length());

        Claims claims;
        try {
            claims = jwtUtil.validateAndExtract(token);
        } catch (JwtException ex) {
            log.warn("[{}] JWT validation failed: {}", requestId, ex.getMessage());
            return unauthorizedResponse(exchange, "Invalid or expired token");
        }

        String userId = jwtUtil.getUserId(claims);
        String role   = jwtUtil.getRole(claims);

        log.info("[{}] Authenticated user={} role={}", requestId, userId, role);

        // Mutate request: add context headers for downstream services
        ServerHttpRequest mutated = request.mutate()
                .header(HEADER_USER_ID,   userId)
                .header(HEADER_USER_ROLE, role)
                .header("X-Request-Id",   requestId)
                // Strip the original Authorization header so downstream services
                // cannot accidentally trust a potentially-invalid token.
                // Comment this out if downstream services need to re-verify the token themselves.
                // .headers(h -> h.remove(HttpHeaders.AUTHORIZATION))
                .build();

        return chain.filter(exchange.mutate().request(mutated).build());
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private boolean isPublicPath(String path) {
        return PUBLIC_PATHS.stream().anyMatch(p -> pathMatcher.match(p, path));
    }

    private ServerWebExchange addRequestId(ServerWebExchange exchange, String requestId) {
        return exchange.mutate()
                .request(exchange.getRequest().mutate()
                        .header("X-Request-Id", requestId)
                        .build())
                .build();
    }

    private Mono<Void> unauthorizedResponse(ServerWebExchange exchange, String message) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
        byte[] body = ("{\"error\":\"Unauthorized\",\"message\":\"" + message + "\"}").getBytes(StandardCharsets.UTF_8);
        return response.writeWith(Mono.just(response.bufferFactory().wrap(body)));
    }
}
