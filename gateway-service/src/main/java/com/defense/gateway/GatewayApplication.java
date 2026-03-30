package com.defense.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * API Gateway Service — single entry point for the Academic Defense Management System.
 *
 * Responsibilities:
 *  - Reverse-proxy routing to downstream microservices
 *  - Stateless JWT validation (no auth-service call per request)
 *  - Header propagation (X-User-Username, X-User-Roles)
 *  - Centralized CORS, logging, and error handling
 */
@SpringBootApplication
public class GatewayApplication {
    public static void main(String[] args) {
        SpringApplication.run(GatewayApplication.class, args);
    }
}
