package com.defensemanagement.academic.security;

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
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GatewayAuthFilterTest {

    @InjectMocks
    private GatewayAuthFilter gatewayAuthFilter;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    @Mock
    private SecurityContext securityContext;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    void testDoFilterInternalWithValidHeaders() throws ServletException, IOException {
        when(request.getHeader("X-User-Username")).thenReturn("testuser");
        when(request.getHeader("X-User-Roles")).thenReturn("ROLE_ADMIN,ROLE_USER");

        gatewayAuthFilter.doFilterInternal(request, response, filterChain);

        verify(securityContext).setAuthentication(any());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void testDoFilterInternalWithSingleRole() throws ServletException, IOException {
        when(request.getHeader("X-User-Username")).thenReturn("testuser");
        when(request.getHeader("X-User-Roles")).thenReturn("ROLE_USER");

        gatewayAuthFilter.doFilterInternal(request, response, filterChain);

        verify(securityContext).setAuthentication(any());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void testDoFilterInternalWithoutHeaders() throws ServletException, IOException {
        when(request.getHeader("X-User-Username")).thenReturn(null);
        when(request.getHeader("X-User-Roles")).thenReturn(null);

        gatewayAuthFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
    }

    @Test
    void testDoFilterInternalWithEmptyRoles() throws ServletException, IOException {
        when(request.getHeader("X-User-Username")).thenReturn("testuser");
        when(request.getHeader("X-User-Roles")).thenReturn("");

        gatewayAuthFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
    }

    @Test
    void testDoFilterInternalWithMultipleRoles() throws ServletException, IOException {
        when(request.getHeader("X-User-Username")).thenReturn("admin");
        when(request.getHeader("X-User-Roles")).thenReturn("ROLE_ADMIN,ROLE_STUDENT,ROLE_PROFESSOR");

        gatewayAuthFilter.doFilterInternal(request, response, filterChain);

        verify(securityContext).setAuthentication(any());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void testDoFilterInternalProcessesAllRoles() throws ServletException, IOException {
        when(request.getHeader("X-User-Username")).thenReturn("faculty");
        when(request.getHeader("X-User-Roles")).thenReturn("ROLE_PROFESSOR,ROLE_ADMIN");

        gatewayAuthFilter.doFilterInternal(request, response, filterChain);

        verify(securityContext).setAuthentication(any());
        verify(filterChain).doFilter(request, response);
    }
}
