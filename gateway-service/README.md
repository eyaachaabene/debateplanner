auth-service  academic-   room-service  defense-service
# Gateway Service

> **API Gateway for the Academic Defense Management System**

---

## Overview

The **Gateway Service** acts as the single entry point for all client requests in the Academic Defense Management System. It provides secure, stateless, and efficient routing to all backend microservices, handling authentication, authorization, logging, rate limiting, and error handling.

---

## Architecture

```
Client (Browser / Mobile)
         │
         ▼
 ┌───────────────┐
 │ gateway-service│  :8080  ← this service
 └───────┬───────┘
         │  routes by path prefix
    ┌────┴───────────────────────────────────┐
    │            │           │               │
    ▼            ▼           ▼               ▼
auth-service  academic-   room-service  defense-service
  :8081       service       :8083          :8084
              :8082
```

---

## Key Features

- **Reverse Proxy Routing**: Configurable via `application.yml` for all backend services
- **JWT Authentication**: Stateless validation and user context propagation
- **Request/Response Logging**: Centralized logging for all traffic
- **Rate Limiting**: In-memory by default, Redis-ready for production
- **CORS Handling**: Global configuration for secure cross-origin requests
- **Circuit Breaker**: Fallbacks for downstream service failures
- **Global Error Handling**: Consistent error responses

---

## Security Flow

1. **Login**: `POST /api/v1/auth/login` → forwarded to auth-service (no JWT required)
2. **Token Issuance**: Auth-service returns JWT
3. **Authenticated Requests**: Gateway validates JWT, extracts user info, adds headers, and forwards
4. **Public Routes**: `/api/v1/auth/login`, `/api/v1/auth/register`, `/actuator/**` (no token required)

---

## Environment Variables

| Variable                | Description                                 |
|-------------------------|---------------------------------------------|
| `JWT_SECRET`            | Shared secret for JWT validation            |
| `AUTH_SERVICE_URL`      | Auth service base URL                       |
| `ACADEMIC_SERVICE_URL`  | Academic service base URL                   |
| `ROOM_SERVICE_URL`      | Room service base URL                       |
| `DEFENSE_SERVICE_URL`   | Defense service base URL                    |
| `ALLOWED_ORIGINS`       | Comma-separated CORS origins                |
| `SPRING_PROFILES_ACTIVE`| Active Spring profile                       |

---

## Running Locally

### With Maven

```bash
cp .env.example .env
mvn spring-boot:run
```

### With Docker Compose

```bash
cp .env.example .env
docker compose up --build
```

Gateway available at [http://localhost:8080](http://localhost:8080)

---

## Route Reference

| Path prefix           | Target service         |
|----------------------|-----------------------|
| `/api/v1/auth/**`       | auth-service:8085     |
| `/api/v1/students/**`   | academic-service:8082 |
| `/api/v1/professors/**` | academic-service:8082 |
| `/api/v1/rooms/**`      | room-service:8083     |
| `/api/v1/defenses/**`   | defense-service:8084  |

---

## Project Structure

```
gateway-service/
├── src/main/java/com/defense/gateway/
│   ├── GatewayApplication.java
│   ├── config/
│   │   ├── CorsConfig.java
│   │   ├── FallbackController.java
│   │   ├── GatewayConfig.java
│   │   └── GlobalErrorHandler.java
│   ├── filter/
│   │   ├── JwtAuthenticationFilter.java
│   │   ├── LoggingFilter.java
│   │   └── RateLimitFilter.java
│   └── util/
│       └── JwtUtil.java
├── src/main/resources/
│   └── application.yml
├── src/test/... (unit + integration tests)
├── Dockerfile
├── docker-compose.yml
├── .env.example
└── pom.xml
```

---

## JWT Claim Contract

| Claim   | Type         | Description                        |
|---------|--------------|------------------------------------|
| `sub`   | string       | Username or email                  |
| `userId`| string/long  | User's primary key                 |
| `role`  | string       | `ADMIN`, `PROFESSOR`, `STUDENT`    |
| `exp`   | numeric      | Expiration timestamp               |

If `userId` is absent, falls back to `sub`.

---

## Health Check

```
GET http://localhost:8080/actuator/health
```

---

## Best Practices & Notes

- **Stateless**: No database, all logic is stateless
- **Local JWT Validation**: No per-request call to auth-service
- **No Business Logic**: All domain logic is in downstream services
- **Production Ready**: Swap in Redis for distributed rate limiting

---

## License

This project is part of the Academic Defense Management System.

---

## Authors

- Adam (Gateway Service)
- Eya Achaabene (Defense Planner)

---

## See Also

- [defense-planner/README.md](defense-planner/README.md) for microservices details
