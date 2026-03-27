# DebatePlanner

A defense management system built with Spring Boot microservices.



## Auth Service Features

- **JWT Authentication**: Access tokens (15 min) and refresh tokens (7 days)
- **User Registration**: Create new users with roles
- **Login**: Authenticate with username/password
- **role Management**: ROLE_ADMIN, ROLE_PROFESSOR, ROLE_STUDENT
- **Spring Security**: Stateless session management with JWT filter
- **PostgreSQL**: Persistent data storage
- **JPA/Hibernate**: ORM for database operations
- **Comprehensive Tests**: Unit tests covering all layers

## Technologies

- **Java 21**
- **Spring Boot 4.0.5**
- **Spring Security**
- **Spring Data JPA**
- **PostgreSQL**
- **jjwt 0.11.5** (JWT library)
- **Lombok**
- **Maven**
- **JUnit 5** & **Mockito**

## Getting Started

### Prerequisites

- Java 21+
- Maven 3.8+
- PostgreSQL 14+

### Setup & Run

1. **Navigate to auth-service:**
   ```bash
   cd auth-service
   ```

2. **Configure database** in `src/main/resources/application.yaml`:
   ```yaml
   spring:
     datasource:
       url: jdbc:postgresql://localhost:5432/auth_db
       username: auth_user
       password: auth_pass
   ```

3. **Run with Maven:**
   ```bash
   mvn spring-boot:run
   ```

   Server starts on `http://localhost:8082`

4. **Run tests:**
   ```bash
   mvn test
   ```

## API Endpoints

Base URL: `http://localhost:8082/api/v1/auth`

### Register
```
POST /register
Content-Type: application/json

{
  "username": "john_doe",
  "password": "password123",
  "roles": ["ROLE_STUDENT"]
}
```

### Login
```
POST /login
Content-Type: application/json

{
  "username": "john_doe",
  "password": "password123"
}
```

### Refresh Token
```
POST /refresh-token
Content-Type: application/json

{
  "refreshToken": "eyJhbGciOiJIUzI1NiJ9..."
}
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
