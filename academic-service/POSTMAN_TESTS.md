# Academic Service - Postman Tests

This directory contains Postman collections for testing the Academic Service API endpoints.

## Files Included

1. **postman_collection.json** - Complete API collection with all Student and Professor endpoints
2. **postman_environment.json** - Environment variables for easy configuration

## Getting Started

### Prerequisites
- [Postman](https://www.postman.com/downloads/) installed
- Academic Service running on `http://localhost:8082`

### Import Instructions

1. **Import Collection:**
   - Open Postman
   - Click `File` → `Import`
   - Select `postman_collection.json`
   - Click `Import`

2. **Import Environment:**
   - Open Postman
   - Click `Manage Environments` (gear icon, top right)
   - Click `Import`
   - Select `postman_environment.json`
   - Click `Import`

3. **Select Environment:**
   - Top right dropdown (currently shows "No Environment")
   - Select `Academic Service Environment`

## Environment Variables

The following variables are pre-configured:

| Variable | Default | Description |
|----------|---------|-------------|
| `base_url` | `http://localhost:8082` | API base URL |
| `admin_username` | `admin` | Admin user for headers |
| `admin_roles` | `ROLE_ADMIN` | Admin roles for headers |
| `user_username` | `user` | Regular user for headers |
| `user_roles` | `ROLE_USER` | Regular user roles for headers |
| `student_id` | `1` | Sample student ID |
| `student_user_id` | `100` | Sample student user ID |
| `professor_id` | `1` | Sample professor ID |
| `professor_user_id` | `200` | Sample professor user ID |

## API Endpoints

### Student Endpoints

| Method | Endpoint | Description | Auth |
|--------|----------|-------------|------|
| `POST` | `/api/v1/students` | Create student | ADMIN |
| `GET` | `/api/v1/students` | Get all students | USER |
| `GET` | `/api/v1/students/{id}` | Get student by ID | USER |
| `GET` | `/api/v1/students/user/{userId}` | Get student by User ID | USER |
| `GET` | `/api/v1/students?major=XXX` | Get students by major | USER |
| `GET` | `/api/v1/students?level=X` | Get students by level | USER |
| `GET` | `/api/v1/students?major=XXX&level=X` | Get students by major and level | USER |
| `PUT` | `/api/v1/students/{id}` | Update student | ADMIN |
| `DELETE` | `/api/v1/students/{id}` | Delete student | ADMIN |

### Professor Endpoints

| Method | Endpoint | Description | Auth |
|--------|----------|-------------|------|
| `POST` | `/api/v1/professors` | Create professor | ADMIN |
| `GET` | `/api/v1/professors` | Get all professors | USER |
| `GET` | `/api/v1/professors/{id}` | Get professor by ID | USER |
| `GET` | `/api/v1/professors/user/{userId}` | Get professor by User ID | USER |
| `PUT` | `/api/v1/professors/{id}` | Update professor | ADMIN |
| `DELETE` | `/api/v1/professors/{id}` | Delete professor | ADMIN |

## Testing Workflow

### 1. Create Test Data
Start by creating a student and professor:
- Run: `Student Endpoints → Create Student`
- Run: `Professor Endpoints → Create Professor`

### 2. Retrieve Endpoints
Test retrieval operations:
- Run: `Student Endpoints → Get All Students`
- Run: `Student Endpoints → Get Student by ID`
- Run: `Professor Endpoints → Get All Professors`

### 3. Query Endpoints
Test filtering operations:
- Run: `Student Endpoints → Get Students by Major`
- Run: `Student Endpoints → Get Students by Level`
- Run: `Student Endpoints → Get Students by Major and Level`

### 4. Update Endpoints
Test modification operations:
- Run: `Student Endpoints → Update Student`
- Run: `Professor Endpoints → Update Professor`

### 5. Delete Endpoints
Test deletion operations:
- Run: `Student Endpoints → Delete Student`
- Run: `Professor Endpoints → Delete Professor`

## Student Majors

Valid major values for students:
- `MASTER_SOFTWARE_ENGINEERING`
- `MASTER_ARTIFICIAL_INTELLIGENCE`
- `MASTER_CYBER_SECURITY`
- `MASTER_CLOUD_COMPUTING`

## Student Levels

Valid level values (1-4 for 4-year masters program):
- `1` - First year
- `2` - Second year
- `3` - Third year
- `4` - Fourth year

## Security Headers

All endpoints require these headers (provided automatically by Postman):
- `X-User-Username` - Username making the request
- `X-User-Roles` - Comma-separated roles (e.g., "ROLE_ADMIN" or "ROLE_USER,ROLE_ADMIN")

## Response Examples

### Create Student Response (201)
```json
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

### Get Students Response (200)
```json
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

### Error Response (404)
```json
{
  "statusCode": 404,
  "error": "Student not found",
  "timestamp": "2026-03-28T12:00:00Z"
}
```

## Tips

1. **Change Base URL:** Update `base_url` in environment for different deployments
2. **Use Variables:** Reference environment variables with `{{variable_name}}`
3. **Test Authentication:** Try requests with different roles to test authorization
4. **View Response:** Click `Body` tab to see formatted response
5. **Send Multiple Requests:** Use Postman's Collection Runner to execute all tests in sequence

## Troubleshooting

### 401 Unauthorized
- Verify `X-User-Username` and `X-User-Roles` headers are present
- Check that environment variables are properly set

### 404 Not Found
- Verify the resource ID is correct
- Ensure the resource was created before trying to retrieve it

### 400 Bad Request
- Check request body format is valid JSON
- Verify all required fields are included
- Ensure email format is valid

### 500 Internal Server Error
- Check server logs for details
- Verify database is running and connected
- Ensure all dependencies are properly initialized
