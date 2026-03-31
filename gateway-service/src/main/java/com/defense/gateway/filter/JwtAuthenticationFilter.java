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
import java.util.UUID;

/**
 * Global JWT authentication filter.
 *
 * <p>Runs on every inbound request. For public routes the request is forwarded
 * immediately. For protected routes the filter:
 * <ol>
 *   <li>Extracts the Bearer token from the Authorization header</li>
 *   <li>Validates the JWT signature and expiration locally (no auth-service call)</li>
 *   <li>Attaches X-User-Username, X-User-Roles and X-Request-Id headers for downstream services</li>
 *   <li>Attaches X-Request-Id for distributed tracing</li>
 *   <li>Returns 401 on security violations</li>
 * </ol>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter implements GlobalFilter, Ordered {

    private static final String BEARER_PREFIX = "Bearer ";
    private static final String HEADER_USER_USERNAME = "X-User-Username";
    private static final String HEADER_USER_ID = "X-User-Id";
    private static final String HEADER_USER_ROLES = "X-User-Roles";
    private static final String HEADER_REQUEST_ID = "X-Request-Id";

    private static final List<String> PUBLIC_PATHS = List.of(
            "/api/v1/auth/login",
            "/api/v1/auth/register",
            "/api/v1/auth/refresh-token",
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
        String requestId = UUID.randomUUID().toString();

        log.info("[{}] {} {}", requestId, request.getMethod(), path);

        // Public routes: bypass JWT validation, but still attach request id
        if (isPublicPath(path)) {
            return chain.filter(addRequestId(exchange, requestId));
        }

        // Protected routes: require and validate JWT
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

        String username = jwtUtil.getUsername(claims);
        String userId = jwtUtil.getUserId(claims);
        String roles = jwtUtil.getRoles(claims);

        log.info("[{}] Authenticated username={} userId={} roles={}", requestId, username, userId, roles);

        // Mutate request: add context headers for downstream services
        ServerHttpRequest mutatedRequest = request.mutate()
                .header(HEADER_USER_USERNAME, username)
                .header(HEADER_USER_ID, userId)
                .header(HEADER_USER_ROLES, roles)
                .header(HEADER_REQUEST_ID, requestId)
                .build();

        return chain.filter(exchange.mutate().request(mutatedRequest).build());
    }

    private boolean isPublicPath(String path) {
        return PUBLIC_PATHS.stream().anyMatch(pattern -> pathMatcher.match(pattern, path));
    }

    private ServerWebExchange addRequestId(ServerWebExchange exchange, String requestId) {
        ServerHttpRequest mutatedRequest = exchange.getRequest().mutate()
                .header(HEADER_REQUEST_ID, requestId)
                .build();

        return exchange.mutate()
                .request(mutatedRequest)
                .build();
    }

    private Mono<Void> unauthorizedResponse(ServerWebExchange exchange, String message) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        String json = "{\"error\":\"Unauthorized\",\"message\":\"" + message + "\"}";
        byte[] body = json.getBytes(StandardCharsets.UTF_8);

        return response.writeWith(Mono.just(response.bufferFactory().wrap(body)));
    }
}
