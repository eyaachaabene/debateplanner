package com.defensemanagement.academic.exception;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class GlobalExceptionHandlerTest {

    @Test
    void testResourceNotFoundExceptionConstruction() {
        String message = "Student not found with id: 999";
        ResourceNotFoundException exception = new ResourceNotFoundException(message);

        assertNotNull(exception);
        assertEqual(message, exception.getMessage());
        assertTrue(exception instanceof RuntimeException);
    }

    @Test
    void testApiErrorBuilding() {
        LocalDateTime now = LocalDateTime.now();
        List<String> errors = List.of("Error 1", "Error 2");

        ApiError apiError = ApiError.builder()
                .status(404)
                .message("Not found")
                .timestamp(now)
                .errors(errors)
                .build();

        assertNotNull(apiError);
        assertEqual(404, apiError.getStatus());
        assertEqual("Not found", apiError.getMessage());
        assertEqual(2, apiError.getErrors().size());
    }

    @Test
    void testApiErrorWithNullErrors() {
        LocalDateTime now = LocalDateTime.now();
        ApiError apiError = ApiError.builder()
                .status(500)
                .message("Internal server error")
                .timestamp(now)
                .errors(null)
                .build();

        assertNotNull(apiError);
        assertNull(apiError.getErrors());
        assertEqual(500, apiError.getStatus());
    }

    @Test
    void testApiErrorValidationError() {
        LocalDateTime now = LocalDateTime.now();
        List<String> errors = List.of("firstName: must not be blank", "level: must be between 1 and 5");

        ApiError apiError = ApiError.builder()
                .status(400)
                .message("Validation failed")
                .timestamp(now)
                .errors(errors)
                .build();

        assertNotNull(apiError);
        assertEqual(400, apiError.getStatus());
        assertEqual("Validation failed", apiError.getMessage());
        assertTrue(apiError.getErrors().contains("firstName: must not be blank"));
    }

    private void assertEqual(Object expected, Object actual) {
        assertEquals(expected, actual);
    }
}
