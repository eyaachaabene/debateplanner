package com.defensemanagement.auth.service.impl;

import com.defensemanagement.auth.dto.AuthResponse;
import com.defensemanagement.auth.dto.ChangePasswordRequest;
import com.defensemanagement.auth.dto.LoginRequest;
import com.defensemanagement.auth.dto.RefreshRequest;
import com.defensemanagement.auth.dto.RegisterRequest;
import com.defensemanagement.auth.entity.ERole;
import com.defensemanagement.auth.entity.Role;
import com.defensemanagement.auth.entity.User;
import com.defensemanagement.auth.repository.RoleRepository;
import com.defensemanagement.auth.repository.UserRepository;
import com.defensemanagement.auth.security.JwtService;
import com.defensemanagement.auth.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    @Override
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException("Username already exists");
        }

        Set<Role> roles = resolveRoles(request.getRoles());

        User user = User.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .roles(roles)
                .build();

        userRepository.save(user);

        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(900000)
                .mustChangePassword(user.isMustChangePassword())
                .build();
    }

    @Override
    public Long registerAndGetId(String username, String rawPassword, String role) {
        if (userRepository.existsByUsername(username)) {
            throw new IllegalArgumentException("Username already exists: " + username);
        }

        ERole eRole = ERole.valueOf(role);
        Role roleEntity = roleRepository.findByName(eRole)
                .orElseThrow(() -> new IllegalArgumentException("Role not found: " + role));

        User user = User.builder()
                .username(username)
                .password(passwordEncoder.encode(rawPassword))
                .roles(Set.of(roleEntity))
                .mustChangePassword(true)
                .build();

        return userRepository.save(user).getId();
    }

    @Override
    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword()
                )
        );

        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(900000)
                .mustChangePassword(user.isMustChangePassword())
                .build();
    }

    @Override
    public void changePassword(String username, ChangePasswordRequest request) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + username));

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new IllegalArgumentException("Current password is incorrect");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        user.setMustChangePassword(false);
        userRepository.save(user);
    }

    @Override
    public AuthResponse refreshToken(RefreshRequest request) {
        String refreshToken = request.getRefreshToken();

        // Extract username from refresh token
        String username = jwtService.extractUsername(refreshToken);

        // Validate refresh token
        if (!jwtService.isTokenValid(refreshToken, username)) {
            throw new IllegalArgumentException("Invalid refresh token");
        }

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        String newAccessToken = jwtService.generateAccessToken(user);

        return AuthResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(900000)
            .mustChangePassword(user.isMustChangePassword())
                .build();
    }

    @Override
    public void logout(String token) {
        // TODO: implement token blacklist
    }

    private Set<Role> resolveRoles(Set<String> roleNames) {
        Set<Role> roles = new HashSet<>();

        if (roleNames == null || roleNames.isEmpty()) {
            Role studentRole = roleRepository.findByName(ERole.STUDENT)
                    .orElseThrow(() -> new IllegalArgumentException("Role not found: STUDENT"));
            roles.add(studentRole);
            return roles;
        }

        for (String roleName : roleNames) {
            ERole eRole = ERole.valueOf(roleName);
            Role role = roleRepository.findByName(eRole)
                    .orElseThrow(() -> new IllegalArgumentException("Role not found: " + roleName));
            roles.add(role);
        }

        return roles;
    }
}
