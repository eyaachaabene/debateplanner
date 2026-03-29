# Auth Service

A JWT-based authentication service for the DebatePlanner defense management system.

## Features

- **JWT Authentication**: Access tokens (15 min) and refresh tokens (7 days)
- **User Registration**: Create new users with roles
- **Login**: Authenticate with username/password
- **role Management**: ROLE_ADMIN, ROLE_PROFESSOR, ROLE_STUDENT
- **Spring Security**: Stateless session management with JWT filter
- **PostgreSQL**: Persistent data storage
- **JPA/Hibernate**: ORM for database operations
- **Comprehensive Tests**: 85+ unit tests and Postman API tests covering all layers

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

## Testing

### Unit Tests
Run all unit tests with Mockito:
```bash
mvn test
```

### Postman Tests
Import the Postman collection to manually test all endpoints:
- Collection: `postman_collection.json`
- Environment: `postman_environment.json`

## Database

PostgreSQL database with tables for:
- Users (credentials and roles)
- Audit logs (login/logout activities)

Create database:
```sql
CREATE DATABASE auth_db;
CREATE USER auth_user WITH PASSWORD 'auth_pass';
GRANT ALL PRIVILEGES ON DATABASE auth_db TO auth_user;
```

## Security

- Passwords stored as bcrypt hashes
- JWT tokens signed with HS256 algorithm
- CORS disabled for backend services
- CSRF protection enabled
