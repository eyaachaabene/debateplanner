package com.defensemanagement.auth.service;

import com.defensemanagement.auth.dto.AuthResponse;
import com.defensemanagement.auth.dto.LoginRequest;
import com.defensemanagement.auth.dto.RefreshRequest;
import com.defensemanagement.auth.dto.RegisterRequest;
import com.defensemanagement.auth.entity.ERole;
import com.defensemanagement.auth.entity.Role;
import com.defensemanagement.auth.entity.User;
import com.defensemanagement.auth.repository.RoleRepository;
import com.defensemanagement.auth.repository.UserRepository;
import com.defensemanagement.auth.security.JwtService;
import com.defensemanagement.auth.service.impl.AuthServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AuthServiceImpl authService;

    private RegisterRequest registerRequest;
    private LoginRequest loginRequest;
    private RefreshRequest refreshRequest;
    private User user;
    private Role studentRole;

    @BeforeEach
    void setUp() {
        studentRole = new Role();
        studentRole.setId(1L);
        studentRole.setName(ERole.ROLE_STUDENT);

        user = User.builder()
                .id(1L)
                .username("testuser")
                .password("encodedPassword")
                .roles(Set.of(studentRole))
                .build();

        registerRequest = new RegisterRequest();
        registerRequest.setUsername("testuser");
        registerRequest.setPassword("password123");
        registerRequest.setRoles(null);

        loginRequest = new LoginRequest();
        loginRequest.setUsername("testuser");
        loginRequest.setPassword("password123");

        refreshRequest = new RefreshRequest();
        refreshRequest.setRefreshToken("refreshToken123");
    }

    @Test
    void testRegisterSuccess() {
        when(userRepository.existsByUsername("testuser")).thenReturn(false);
        when(roleRepository.findByName(ERole.ROLE_STUDENT)).thenReturn(Optional.of(studentRole));
        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(jwtService.generateAccessToken(any(User.class))).thenReturn("accessToken");
        when(jwtService.generateRefreshToken(any(User.class))).thenReturn("refreshToken");

        AuthResponse response = authService.register(registerRequest);

        assertNotNull(response);
        assertEquals("accessToken", response.getAccessToken());
        assertEquals("refreshToken", response.getRefreshToken());
        assertEquals("Bearer", response.getTokenType());
        assertEquals(900000, response.getExpiresIn());

        verify(userRepository).existsByUsername("testuser");
        verify(roleRepository).findByName(ERole.ROLE_STUDENT);
        verify(userRepository).save(any(User.class));
    }

    @Test
    void testRegisterUserAlreadyExists() {
        when(userRepository.existsByUsername("testuser")).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> authService.register(registerRequest));
        verify(userRepository).existsByUsername("testuser");
    }

    @Test
    void testRegisterWithMultipleRoles() {
        Role adminRole = new Role();
        adminRole.setId(2L);
        adminRole.setName(ERole.ROLE_ADMIN);

        Set<String> roles = Set.of("ROLE_ADMIN", "ROLE_STUDENT");
        registerRequest.setRoles(roles);

        when(userRepository.existsByUsername("testuser")).thenReturn(false);
        when(roleRepository.findByName(ERole.ROLE_ADMIN)).thenReturn(Optional.of(adminRole));
        when(roleRepository.findByName(ERole.ROLE_STUDENT)).thenReturn(Optional.of(studentRole));
        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(jwtService.generateAccessToken(any(User.class))).thenReturn("accessToken");
        when(jwtService.generateRefreshToken(any(User.class))).thenReturn("refreshToken");

        AuthResponse response = authService.register(registerRequest);

        assertNotNull(response);
        verify(roleRepository).findByName(ERole.ROLE_ADMIN);
        verify(roleRepository).findByName(ERole.ROLE_STUDENT);
    }

    @Test
    void testRegisterRoleNotFound() {
        registerRequest.setRoles(Set.of("ROLE_ADMIN"));
        when(userRepository.existsByUsername("testuser")).thenReturn(false);
        when(roleRepository.findByName(ERole.ROLE_ADMIN)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> authService.register(registerRequest));
    }

    @Test
    void testLoginSuccess() {
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(new UsernamePasswordAuthenticationToken("testuser", "password123"));
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(jwtService.generateAccessToken(user)).thenReturn("accessToken");
        when(jwtService.generateRefreshToken(user)).thenReturn("refreshToken");

        AuthResponse response = authService.login(loginRequest);

        assertNotNull(response);
        assertEquals("accessToken", response.getAccessToken());
        assertEquals("refreshToken", response.getRefreshToken());
        assertEquals("Bearer", response.getTokenType());

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(userRepository).findByUsername("testuser");
    }

    @Test
    void testLoginUserNotFound() {
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(new UsernamePasswordAuthenticationToken("testuser", "password123"));
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> authService.login(loginRequest));
    }

    @Test
    void testRefreshTokenSuccess() {
        when(jwtService.extractUsername("refreshToken123")).thenReturn("testuser");
        when(jwtService.isTokenValid("refreshToken123", "testuser")).thenReturn(true);
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(jwtService.generateAccessToken(user)).thenReturn("newAccessToken");

        AuthResponse response = authService.refreshToken(refreshRequest);

        assertNotNull(response);
        assertEquals("newAccessToken", response.getAccessToken());
        assertEquals("refreshToken123", response.getRefreshToken());
        assertEquals("Bearer", response.getTokenType());

        verify(jwtService).extractUsername("refreshToken123");
        verify(jwtService).isTokenValid("refreshToken123", "testuser");
    }

    @Test
    void testRefreshTokenInvalid() {
        when(jwtService.extractUsername("refreshToken123")).thenReturn("testuser");
        when(jwtService.isTokenValid("refreshToken123", "testuser")).thenReturn(false);

        assertThrows(IllegalArgumentException.class, () -> authService.refreshToken(refreshRequest));
    }

    @Test
    void testRefreshTokenUserNotFound() {
        when(jwtService.extractUsername("refreshToken123")).thenReturn("testuser");
        when(jwtService.isTokenValid("refreshToken123", "testuser")).thenReturn(true);
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> authService.refreshToken(refreshRequest));
    }

    @Test
    void testLogout() {
        assertDoesNotThrow(() -> authService.logout("token123"));
    }
}
