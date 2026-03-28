# DebatePlanner

A defense management system built with Spring Boot microservices architecture.

## Services

**Auth Service** - JWT-based authentication and user management
- User registration, login, token refresh
- Role-based access control (ADMIN, PROFESSOR, STUDENT)
- 85+ comprehensive unit tests and Postman API tests

**Academic Service** - Student and professor academic information management
- Student/professor CRUD operations
- Query filtering by major, level, email, and userId
- MapStruct for DTO mapping
- 85+ comprehensive unit tests and Postman API tests

## Core Technologies

- Java 21
- Spring Boot 4.0.5
- Spring Security with JWT and Gateway auth
- Spring Data JPA
- PostgreSQL
- MapStruct
- JUnit 5, Mockito
- Maven

## Quick Start

### Prerequisites
- Java 21+
- Maven 3.8+
- PostgreSQL 14+

### Run Services

Each service runs independently on port 8082 (when deployed separately):

Auth Service:
```bash
cd auth-service
mvn spring-boot:run
```

Academic Service:
```bash
cd academic-service
mvn spring-boot:run
```

### Run Tests

Both services come fully tested with unit tests and Postman collections:

```bash
mvn test                    # Run unit tests
```

Postman collections and environments are included for API testing.

## Documentation

See individual README files in each service directory for detailed information:
- `auth-service/README.md` - Auth service documentation
- `academic-service/README.md` - Academic service documentation
- `academic-service/POSTMAN_TESTS.md` - Postman API testing guide

## Testing

**Unit Tests**: All layers covered with Mockito (85+ tests per service)
- Controllers, Services, Mappers, Repositories, Security, Exception handling

**Postman Tests**: Complete API endpoint collections
- Auth Service: auth endpoints (register, login, refresh-token)
- Academic Service: student and professor endpoints

Both services are production-ready with comprehensive test coverage.
```

### Logout
```
POST /logout
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
```

## JWT Configuration

Set these environment variables or update `application.yaml`:

```yaml
application:
  security:
    jwt:
      secret-key: ${JWT_SECRET}  # 256-bit base64-encoded key
      access-token-expiration: 900000    # 15 minutes
      refresh-token-expiration: 604800000 # 7 days
```

## Project Statistics

- **Total Files**: 19 main classes + 6 test classes
- **Lines of Code**: ~2,500 (main) + ~1,200 (tests)
- **Test Coverage**: 30 unit tests (all passing)
- **Build Time**: ~7 seconds

## Database Schema

### Tables

- **users**: User accounts with credentials and role associations
- **roles**: Role definitions (ADMIN, PROFESSOR, STUDENT)
- **user_roles**: Many-to-many relationship between users and roles

## Error Handling

Global exception handler with proper HTTP status codes:
- `400 Bad Request`: Validation failures, duplicate usernames
- `401 Unauthorized`: Invalid credentials
- `404 Not Found`: User not found
- `500 Internal Server Error`: Unexpected errors

## License

This project is part of the Defense Management System.

## Author

Eya Achaabene
