package com.defensemanagement.defense.exception;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GlobalExceptionHandlerTest {

    @InjectMocks
    private GlobalExceptionHandler globalExceptionHandler;

    @Mock
    private MethodArgumentNotValidException methodArgumentNotValidException;

    @Mock
    private BindingResult bindingResult;

    @Test
    void testHandleMethodArgumentNotValid() {
        when(methodArgumentNotValidException.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldErrors()).thenReturn(List.of(
                new FieldError("defenseRequest", "projectTitle", "Project title is required"),
                new FieldError("defenseRequest", "studentId", "Student id is required")
        ));

        ResponseEntity<ApiError> response = globalExceptionHandler.handleMethodArgumentNotValid(methodArgumentNotValidException);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Validation failed", response.getBody().getMessage());
        assertEquals(2, response.getBody().getErrors().size());
    }

    @Test
    void testHandleResourceNotFound() {
        ResponseEntity<ApiError> response =
                globalExceptionHandler.handleResourceNotFound(new ResourceNotFoundException("Defense not found"));

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Defense not found", response.getBody().getMessage());
    }

    @Test
    void testHandleIllegalArgument() {
        ResponseEntity<ApiError> response =
                globalExceptionHandler.handleIllegalArgument(new IllegalArgumentException("Invalid jury assignment"));

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Invalid jury assignment", response.getBody().getMessage());
    }
}
