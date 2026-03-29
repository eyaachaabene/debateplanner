# Academic Service

A microservice for managing student and professor academic information in the DebatePlanner defense management system.

## Features

- **Student Management**: Create, read, update, delete student records
- **Professor Management**: Create, read, update, delete professor records
- **Query by Filters**: Search students by major, level, email, userId
- **DTO/Entity Mapping**: MapStruct for clean separation of concerns
- **Gateway Authentication**: Header-based authentication via API Gateway (X-User-Username, X-User-Roles)
- **Role-Based Access Control**: @PreAuthorize for method-level security
- **PostgreSQL**: Persistent data storage with JPA/Hibernate
- **Comprehensive Tests**: 85+ unit tests and Postman API tests covering all layers

## Technologies

- **Java 21**
- **Spring Boot 4.0.5**
- **Spring Security**
- **Spring Data JPA**
- **MapStruct 1.5.5.Final** (DTO mapping)
- **PostgreSQL**
- **Lombok**
- **Maven**
- **JUnit 5** & **Mockito**

## Getting Started

### Prerequisites

- Java 21+
- Maven 3.8+
- PostgreSQL 14+

### Setup & Run

1. **Navigate to academic-service:**
   ```bash
   cd academic-service
   ```

2. **Configure database** in `src/main/resources/application.yaml`:
   ```yaml
   spring:
     datasource:
       url: jdbc:postgresql://localhost:5432/academic_db
       username: academic_user
       password: academic_pass
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

Base URL: `http://localhost:8082/api/v1`

### Student Endpoints

| Method | Endpoint | Description | Auth |
|--------|----------|-------------|------|
| POST | `/students` | Create student | ADMIN |
| GET | `/students` | Get all students | USER |
| GET | `/students/{id}` | Get student by ID | USER |
| GET | `/students/user/{userId}` | Get student by User ID | USER |
| GET | `/students?major=XXX` | Get students by major | USER |
| GET | `/students?level=X` | Get students by level | USER |
| GET | `/students?major=XXX&level=X` | Get students by major and level | USER |
| PUT | `/students/{id}` | Update student | ADMIN |
| DELETE | `/students/{id}` | Delete student | ADMIN |

### Professor Endpoints

| Method | Endpoint | Description | Auth |
|--------|----------|-------------|------|
| POST | `/professors` | Create professor | ADMIN |
| GET | `/professors` | Get all professors | USER |
| GET | `/professors/{id}` | Get professor by ID | USER |
| GET | `/professors/user/{userId}` | Get professor by User ID | USER |
| PUT | `/professors/{id}` | Update professor | ADMIN |
| DELETE | `/professors/{id}` | Delete professor | ADMIN |

## Request/Response Examples

### Create Student
```
POST /api/v1/students
Content-Type: application/json
X-User-Username: admin
X-User-Roles: ROLE_ADMIN

{
  "userId": 100,
  "firstName": "John",
  "lastName": "Doe",
  "email": "john.doe@university.edu",
  "major": "MASTER_SOFTWARE_ENGINEERING",
  "level": 2
}

Response (201):
{
  "id": 1,
  "userId": 100,
  "firstName": "John",
  "lastName": "Doe",
  "email": "john.doe@university.edu",
  "major": "MASTER_SOFTWARE_ENGINEERING",
  "level": 2
}
```

### Get All Students
```
GET /api/v1/students
X-User-Username: user
X-User-Roles: ROLE_USER

Response (200):
[
  {
    "id": 1,
    "userId": 100,
    "firstName": "John",
    "lastName": "Doe",
    "email": "john.doe@university.edu",
    "major": "MASTER_SOFTWARE_ENGINEERING",
    "level": 2
  }
]
```

## Testing

### Unit Tests
All layers are thoroughly tested with Mockito unit tests:
- **Controllers**: 15 tests for API endpoints
- **Services**: 25 tests for business logic
- **Mappers**: 12 tests for DTO/Entity conversion
- **Repositories**: 19 tests for custom queries
- **Security**: 9 tests for authentication/authorization
- **Exception Handling**: 4 tests for error scenarios
- **Application**: 1 test for startup

**Total: 85+ unit tests** - Run with:
```bash
mvn test
```

### Postman Tests
Import the Postman collection for manual API testing:
- Collection: `postman_collection.json`
- Environment: `postman_environment.json`

All endpoints can be tested with proper authentication headers and request bodies.

## Database

PostgreSQL database with tables for:
- Students (userId, firstName, lastName, email, major, level)
- Professors (userId, firstName, lastName, email)

Create database:
```sql
CREATE DATABASE academic_db;
CREATE USER academic_user WITH PASSWORD 'academic_pass';
GRANT ALL PRIVILEGES ON DATABASE academic_db TO academic_user;
```

## Student Majors

- MASTER_SOFTWARE_ENGINEERING
- MASTER_ARTIFICIAL_INTELLIGENCE
- MASTER_CYBER_SECURITY
- MASTER_CLOUD_COMPUTING

## Security

- Gateway authentication via request headers (X-User-Username, X-User-Roles)
- Role-based method security with @PreAuthorize
- CSRF disabled for stateless API
- MapStruct for secure DTO mapping
