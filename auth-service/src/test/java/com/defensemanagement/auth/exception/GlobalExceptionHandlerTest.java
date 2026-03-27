package com.defensemanagement.auth.exception;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GlobalExceptionHandlerTest {

    @InjectMocks
    private GlobalExceptionHandler globalExceptionHandler;

    @BeforeEach
    void setUp() {
    }

    @Test
    void testHandleMethodArgumentNotValid() {
        MethodArgumentNotValidException exception = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);

        List<FieldError> fieldErrors = new ArrayList<>();
        fieldErrors.add(new FieldError("object", "username", "must not be blank"));
        fieldErrors.add(new FieldError("object", "password", "size must be between 8 and 100"));

        when(exception.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldErrors()).thenReturn(fieldErrors);

        ResponseEntity<ApiError> response = globalExceptionHandler.handleMethodArgumentNotValid(exception);

        assertEquals(HttpStatus.BAD_REQUEST.value(), response.getBody().getStatus());
        assertEquals("Validation failed", response.getBody().getMessage());
        assertEquals(2, response.getBody().getErrors().size());
        assertNotNull(response.getBody().getTimestamp());
    }

    @Test
    void testHandleBadCredentials() {
        BadCredentialsException exception = new BadCredentialsException("Bad credentials");

        ResponseEntity<ApiError> response = globalExceptionHandler.handleBadCredentials(exception);

        assertEquals(HttpStatus.UNAUTHORIZED.value(), response.getBody().getStatus());
        assertEquals("Invalid username or password", response.getBody().getMessage());
        assertNotNull(response.getBody().getTimestamp());
    }

    @Test
    void testHandleUsernameNotFound() {
        UsernameNotFoundException exception = new UsernameNotFoundException("User not found");

        ResponseEntity<ApiError> response = globalExceptionHandler.handleUsernameNotFound(exception);

        assertEquals(HttpStatus.NOT_FOUND.value(), response.getBody().getStatus());
        assertEquals("User not found", response.getBody().getMessage());
        assertNotNull(response.getBody().getTimestamp());
    }

    @Test
    void testHandleIllegalArgument() {
        IllegalArgumentException exception = new IllegalArgumentException("Invalid role");

        ResponseEntity<ApiError> response = globalExceptionHandler.handleIllegalArgument(exception);

        assertEquals(HttpStatus.BAD_REQUEST.value(), response.getBody().getStatus());
        assertEquals("Invalid role", response.getBody().getMessage());
        assertNotNull(response.getBody().getTimestamp());
    }

    @Test
    void testHandleException() {
        Exception exception = new Exception("Internal server error");

        ResponseEntity<ApiError> response = globalExceptionHandler.handleException(exception);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), response.getBody().getStatus());
        assertEquals("An internal error occurred", response.getBody().getMessage());
        assertNotNull(response.getBody().getTimestamp());
    }

    @Test
    void testApiErrorTimestampIsSet() {
        IllegalArgumentException exception = new IllegalArgumentException("Test error");
        LocalDateTime beforeCall = LocalDateTime.now();

        ResponseEntity<ApiError> response = globalExceptionHandler.handleIllegalArgument(exception);

        LocalDateTime afterCall = LocalDateTime.now();
        LocalDateTime returnedTimestamp = response.getBody().getTimestamp();

        assertNotNull(returnedTimestamp);
        assertTrue(returnedTimestamp.isAfter(beforeCall.minusSeconds(1)));
        assertTrue(returnedTimestamp.isBefore(afterCall.plusSeconds(1)));
    }

    @Test
    void testApiErrorBuilderWithAllFields() {
        ApiError error = ApiError.builder()
                .status(400)
                .message("Test message")
                .timestamp(LocalDateTime.now())
                .errors(List.of("error1", "error2"))
                .build();

        assertEquals(400, error.getStatus());
        assertEquals("Test message", error.getMessage());
        assertEquals(2, error.getErrors().size());
        assertNotNull(error.getTimestamp());
    }
}
