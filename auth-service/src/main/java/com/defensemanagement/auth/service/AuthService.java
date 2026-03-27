package com.defensemanagement.auth.service;

import com.defensemanagement.auth.dto.AuthResponse;
import com.defensemanagement.auth.dto.LoginRequest;
import com.defensemanagement.auth.dto.RefreshRequest;
import com.defensemanagement.auth.dto.RegisterRequest;

public interface AuthService {
    AuthResponse register(RegisterRequest request);

    AuthResponse login(LoginRequest request);

    AuthResponse refreshToken(RefreshRequest request);

    void logout(String token);
}
