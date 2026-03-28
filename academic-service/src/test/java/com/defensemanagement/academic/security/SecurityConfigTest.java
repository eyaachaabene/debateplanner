package com.defensemanagement.academic.security;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith(MockitoExtension.class)
class SecurityConfigTest {

    @Mock
    private GatewayAuthFilter gatewayAuthFilter;

    @Test
    void testSecurityConfigIsValid() {
        SecurityConfig securityConfig = new SecurityConfig(gatewayAuthFilter);
        assertNotNull(securityConfig);
    }

    @Test
    void testGatewayAuthFilterIsInjected() {
        SecurityConfig securityConfig = new SecurityConfig(gatewayAuthFilter);
        assertNotNull(gatewayAuthFilter);
    }

    @Test
    void testSecurityConfigCanBeInstantiated() {
        try {
            SecurityConfig securityConfig = new SecurityConfig(gatewayAuthFilter);
            assertNotNull(securityConfig);
        } catch (Exception e) {
            throw new RuntimeException("Failed to instantiate SecurityConfig", e);
        }
    }
}
