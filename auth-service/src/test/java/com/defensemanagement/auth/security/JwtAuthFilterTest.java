package com.defensemanagement.auth.security;

import com.defensemanagement.auth.entity.ERole;
import com.defensemanagement.auth.entity.Role;
import com.defensemanagement.auth.entity.User;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.io.IOException;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtAuthFilterTest {

    @Mock
    private JwtService jwtService;

    @Mock
    private UserDetailsService userDetailsService;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    @InjectMocks
    private JwtAuthFilter jwtAuthFilter;

    private User testUser;
    private String validToken;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();

        Role role = new Role();
        role.setId(1L);
        role.setName(ERole.ROLE_STUDENT);

        testUser = User.builder()
                .id(1L)
                .username("testuser")
                .password("password123")
                .roles(Set.of(role))
                .build();

        validToken = "valid.jwt.token";
    }

    @Test
    void testDoFilterInternalWithValidToken() throws ServletException, IOException {
        String authHeader = "Bearer " + validToken;

        when(request.getHeader("Authorization")).thenReturn(authHeader);
        when(jwtService.extractUsername(validToken)).thenReturn("testuser");
        when(userDetailsService.loadUserByUsername("testuser")).thenReturn(testUser);
        when(jwtService.isTokenValid(validToken, "testuser")).thenReturn(true);

        jwtAuthFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
    }

    @Test
    void testDoFilterInternalWithNoAuthorizationHeader() throws ServletException, IOException {
        when(request.getHeader("Authorization")).thenReturn(null);

        jwtAuthFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        verify(jwtService, never()).extractUsername(any());
    }

    @Test
    void testDoFilterInternalWithInvalidAuthorizationHeaderFormat() throws ServletException, IOException {
        when(request.getHeader("Authorization")).thenReturn("Basic credentials");

        jwtAuthFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        verify(jwtService, never()).extractUsername(any());
    }

    @Test
    void testDoFilterInternalWithInvalidToken() throws ServletException, IOException {
        String authHeader = "Bearer invalidtoken";

        when(request.getHeader("Authorization")).thenReturn(authHeader);
        when(jwtService.extractUsername("invalidtoken")).thenThrow(new RuntimeException("Invalid token"));

        jwtAuthFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
    }

    @Test
    void testDoFilterInternalWithInvalidTokenValidity() throws ServletException, IOException {
        String authHeader = "Bearer " + validToken;

        when(request.getHeader("Authorization")).thenReturn(authHeader);
        when(jwtService.extractUsername(validToken)).thenReturn("testuser");
        when(userDetailsService.loadUserByUsername("testuser")).thenReturn(testUser);
        when(jwtService.isTokenValid(validToken, "testuser")).thenReturn(false);

        jwtAuthFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
    }

    @Test
    void testDoFilterInternalWithExpiredToken() throws ServletException, IOException {
        String authHeader = "Bearer expiredtoken";

        when(request.getHeader("Authorization")).thenReturn(authHeader);
        when(jwtService.extractUsername("expiredtoken")).thenReturn("testuser");
        when(userDetailsService.loadUserByUsername("testuser")).thenReturn(testUser);
        when(jwtService.isTokenValid("expiredtoken", "testuser")).thenReturn(false);

        jwtAuthFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
    }

    @Test
    void testDoFilterInternalAlwaysCallsFilterChain() throws ServletException, IOException {
        when(request.getHeader("Authorization")).thenReturn(null);

        jwtAuthFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
    }

    @Test
    void testAuthorizationHeaderWithBearerPrefixExtraction() throws ServletException, IOException {
        String authHeader = "Bearer eyJhbGciOiJIUzI1NiJ9";

        when(request.getHeader("Authorization")).thenReturn(authHeader);
        when(jwtService.extractUsername(eq("eyJhbGciOiJIUzI1NiJ9"))).thenReturn("testuser");
        when(userDetailsService.loadUserByUsername("testuser")).thenReturn(testUser);
        when(jwtService.isTokenValid("eyJhbGciOiJIUzI1NiJ9", "testuser")).thenReturn(true);

        jwtAuthFilter.doFilterInternal(request, response, filterChain);

        verify(jwtService).extractUsername("eyJhbGciOiJIUzI1NiJ9");
        verify(filterChain).doFilter(request, response);
    }
}
