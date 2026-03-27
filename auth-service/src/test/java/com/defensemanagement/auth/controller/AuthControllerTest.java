package com.defensemanagement.auth.controller;

import com.defensemanagement.auth.dto.AuthResponse;
import com.defensemanagement.auth.dto.LoginRequest;
import com.defensemanagement.auth.dto.RefreshRequest;
import com.defensemanagement.auth.dto.RegisterRequest;
import com.defensemanagement.auth.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private AuthService authService;

    @InjectMocks
    private AuthController authController;

    private RegisterRequest registerRequest;
    private LoginRequest loginRequest;
    private RefreshRequest refreshRequest;
    private AuthResponse authResponse;

    @BeforeEach
    void setUp() {
        registerRequest = new RegisterRequest();
        registerRequest.setUsername("testuser");
        registerRequest.setPassword("password123");
        registerRequest.setRoles(Set.of("ROLE_STUDENT"));

        loginRequest = new LoginRequest();
        loginRequest.setUsername("testuser");
        loginRequest.setPassword("password123");

        refreshRequest = new RefreshRequest();
        refreshRequest.setRefreshToken("refreshToken123");

        authResponse = AuthResponse.builder()
                .accessToken("accessToken123")
                .refreshToken("refreshToken123")
                .tokenType("Bearer")
                .expiresIn(900000)
                .build();
    }

    @Test
    void testRegisterSuccess() {
        when(authService.register(any(RegisterRequest.class))).thenReturn(authResponse);

        ResponseEntity<AuthResponse> response = authController.register(registerRequest);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("accessToken123", response.getBody().getAccessToken());
        assertEquals("refreshToken123", response.getBody().getRefreshToken());
        assertEquals("Bearer", response.getBody().getTokenType());
        assertEquals(900000, response.getBody().getExpiresIn());

        verify(authService).register(any(RegisterRequest.class));
    }

    @Test
    void testRegisterWithDuplicateUsername() {
        when(authService.register(any(RegisterRequest.class)))
                .thenThrow(new IllegalArgumentException("Username already exists"));

        assertThrows(IllegalArgumentException.class, () -> authController.register(registerRequest));
        verify(authService).register(any(RegisterRequest.class));
    }

    @Test
    void testLoginSuccess() {
        when(authService.login(any(LoginRequest.class))).thenReturn(authResponse);

        ResponseEntity<AuthResponse> response = authController.login(loginRequest);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("accessToken123", response.getBody().getAccessToken());

        verify(authService).login(any(LoginRequest.class));
    }

    @Test
    void testRefreshTokenSuccess() {
        AuthResponse refreshResponse = AuthResponse.builder()
                .accessToken("newAccessToken")
                .refreshToken("refreshToken123")
                .tokenType("Bearer")
                .expiresIn(900000)
                .build();

        when(authService.refreshToken(any(RefreshRequest.class))).thenReturn(refreshResponse);

        ResponseEntity<AuthResponse> response = authController.refreshToken(refreshRequest);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("newAccessToken", response.getBody().getAccessToken());

        verify(authService).refreshToken(any(RefreshRequest.class));
    }

    @Test
    void testLogoutSuccess() {
        doNothing().when(authService).logout("token123");

        ResponseEntity<Void> response = authController.logout("Bearer token123");

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(authService).logout("token123");
    }

    @Test
    void testRegisterReturnsCreatedStatus() {
        when(authService.register(any(RegisterRequest.class))).thenReturn(authResponse);

        ResponseEntity<AuthResponse> response = authController.register(registerRequest);

        assertEquals(201, response.getStatusCode().value());
    }

    @Test
    void testLoginReturnsOkStatus() {
        when(authService.login(any(LoginRequest.class))).thenReturn(authResponse);

        ResponseEntity<AuthResponse> response = authController.login(loginRequest);

        assertEquals(200, response.getStatusCode().value());
    }

    @Test
    void testLogoutReturnsNoContent() {
        doNothing().when(authService).logout("token123");

        ResponseEntity<Void> response = authController.logout("Bearer token123");

        assertEquals(204, response.getStatusCode().value());
    }
}
