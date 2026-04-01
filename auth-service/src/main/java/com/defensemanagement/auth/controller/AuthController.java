package com.defensemanagement.auth.controller;

import com.defensemanagement.auth.dto.AuthResponse;
import com.defensemanagement.auth.dto.ChangePasswordRequest;
import com.defensemanagement.auth.dto.LoginRequest;
import com.defensemanagement.auth.dto.RegisterInternalRequest;
import com.defensemanagement.auth.dto.RegisterInternalResponse;
import com.defensemanagement.auth.dto.RefreshRequest;
import com.defensemanagement.auth.dto.RegisterRequest;
import com.defensemanagement.auth.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    @Value("${internal.secret}")
    private String internalSecret;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.status(201).body(authService.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/register-internal")
    public ResponseEntity<RegisterInternalResponse> registerInternal(
            @Valid @RequestBody RegisterInternalRequest request,
            @RequestHeader("X-Internal-Secret") String secret) {
        if (!internalSecret.equals(secret)) {
            return ResponseEntity.status(403).body(null);
        }

        Long userId = authService.registerAndGetId(request.getUsername(), request.getPassword(), request.getRole());
        return ResponseEntity.status(201).body(new RegisterInternalResponse(userId));
    }

    @PutMapping("/change-password")
    public ResponseEntity<Void> changePassword(
            @Valid @RequestBody ChangePasswordRequest request,
            @RequestHeader("X-User-Username") String username) {
        authService.changePassword(username, request);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<AuthResponse> refreshToken(@Valid @RequestBody RefreshRequest request) {
        return ResponseEntity.ok(authService.refreshToken(request));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            authService.logout(token);
        }
        return ResponseEntity.noContent().build();
    }
}
