package com.defense.gateway.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Lightweight in-memory rate limiter (optional enhancement).
 *
 * <p>Uses a fixed sliding window per client IP. For production, replace with
 * Redis-backed rate limiting via {@code spring-cloud-starter-gateway} request rate limiter.
 *
 * <p>Default: 100 requests per 60-second window per IP.
 */
@Slf4j
@Component
public class RateLimitFilter implements GlobalFilter, Ordered {

    private static final int MAX_REQUESTS_PER_WINDOW = 100;
    private static final long WINDOW_MS = 60_000L;

    // ip -> [count, windowStart]
    private final Map<String, long[]> requestCounts = new ConcurrentHashMap<>();

    @Override
    public int getOrder() {
        return -200; // Before JWT filter
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String clientIp = extractClientIp(exchange);
        long now = System.currentTimeMillis();

        long[] state = requestCounts.compute(clientIp, (ip, current) -> {
            if (current == null || now - current[1] > WINDOW_MS) {
                return new long[]{1, now};
            }
            current[0]++;
            return current;
        });

        if (state[0] > MAX_REQUESTS_PER_WINDOW) {
            log.warn("Rate limit exceeded for IP: {}", clientIp);
            return tooManyRequestsResponse(exchange);
        }

        return chain.filter(exchange);
    }

    private String extractClientIp(ServerWebExchange exchange) {
        String forwarded = exchange.getRequest().getHeaders().getFirst("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        var remoteAddress = exchange.getRequest().getRemoteAddress();
        return remoteAddress != null ? remoteAddress.getAddress().getHostAddress() : "unknown";
    }

    private Mono<Void> tooManyRequestsResponse(ServerWebExchange exchange) {
        var response = exchange.getResponse();
        response.setStatusCode(HttpStatus.TOO_MANY_REQUESTS);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
        byte[] body = "{\"error\":\"Too Many Requests\",\"message\":\"Rate limit exceeded. Try again later.\"}".getBytes(StandardCharsets.UTF_8);
        return response.writeWith(Mono.just(response.bufferFactory().wrap(body)));
    }
}
