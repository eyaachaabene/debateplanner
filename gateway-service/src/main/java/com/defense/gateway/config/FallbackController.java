package com.defense.gateway.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.Map;

/**
 * Fallback controller invoked by circuit breakers when a downstream service
 * is unreachable. Returns a user-friendly 503 response instead of a raw error.
 */
@Slf4j
@RestController
public class FallbackController {

    @RequestMapping("/fallback/auth")
    public Mono<ResponseEntity<Map<String, String>>> authFallback() {
        return fallback("auth-service");
    }

    @RequestMapping("/fallback/academic")
    public Mono<ResponseEntity<Map<String, String>>> academicFallback() {
        return fallback("academic-service");
    }

    @RequestMapping("/fallback/room")
    public Mono<ResponseEntity<Map<String, String>>> roomFallback() {
        return fallback("room-service");
    }

    @RequestMapping("/fallback/defense")
    public Mono<ResponseEntity<Map<String, String>>> defenseFallback() {
        return fallback("defense-service");
    }

    private Mono<ResponseEntity<Map<String, String>>> fallback(String service) {
        log.warn("Circuit breaker triggered for {}", service);
        return Mono.just(ResponseEntity
                .status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(Map.of(
                        "error",   "Service Unavailable",
                        "message", service + " is temporarily unavailable. Please try again later.",
                        "service", service
                )));
    }
}
