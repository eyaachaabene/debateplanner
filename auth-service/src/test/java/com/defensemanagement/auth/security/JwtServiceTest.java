package com.defensemanagement.auth.security;

import com.defensemanagement.auth.entity.ERole;
import com.defensemanagement.auth.entity.Role;
import com.defensemanagement.auth.entity.User;
import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.TestPropertySource;

import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestPropertySource(properties = {
    "application.security.jwt.secret-key=dGhpc2lzYXZlcnlzZWN1cmVzZWNyZXRrZXlmb3JqV1RhY2Nlc3N0b2tlbmdlbmVyYXRpb25hbGdvcml0aG0=",
    "application.security.jwt.access-token-expiration=900000",
    "application.security.jwt.refresh-token-expiration=604800000"
})
class JwtServiceTest {

    @Autowired
    private JwtService jwtService;

    private User testUser;

    @BeforeEach
    void setUp() {
        Role role = new Role();
        role.setId(1L);
        role.setName(ERole.ROLE_STUDENT);

        testUser = User.builder()
                .id(1L)
                .username("testuser")
                .password("password123")
                .roles(Set.of(role))
                .build();
    }

    @Test
    void testGenerateAccessToken() {
        String token = jwtService.generateAccessToken(testUser);

        assertNotNull(token);
        assertFalse(token.isEmpty());
        assertTrue(token.split("\\.").length == 3); // JWT has 3 parts
    }

    @Test
    void testGenerateRefreshToken() {
        String token = jwtService.generateRefreshToken(testUser);

        assertNotNull(token);
        assertFalse(token.isEmpty());
        assertTrue(token.split("\\.").length == 3);
    }

    @Test
    void testExtractUsername() {
        String token = jwtService.generateAccessToken(testUser);
        String username = jwtService.extractUsername(token);

        assertEquals("testuser", username);
    }

    @Test
    void testIsTokenValidWithValidToken() {
        String token = jwtService.generateAccessToken(testUser);
        boolean isValid = jwtService.isTokenValid(token, "testuser");

        assertTrue(isValid);
    }

    @Test
    void testIsTokenValidWithWrongUsername() {
        String token = jwtService.generateAccessToken(testUser);
        boolean isValid = jwtService.isTokenValid(token, "wronguser");

        assertFalse(isValid);
    }

    @Test
    void testIsTokenValidWithInvalidToken() {
        boolean isValid = jwtService.isTokenValid("invalidToken123", "testuser");

        assertFalse(isValid);
    }

    @Test
    void testExtractUsernameFromInvalidToken() {
        assertThrows(JwtException.class, () -> jwtService.extractUsername("invalidToken"));
    }

    @Test
    void testAccessTokenContainsRoles() {
        Role adminRole = new Role();
        adminRole.setId(2L);
        adminRole.setName(ERole.ROLE_ADMIN);

        User userWithRoles = User.builder()
                .id(1L)
                .username("admin")
                .password("password123")
                .roles(Set.of(adminRole))
                .build();

        String token = jwtService.generateAccessToken(userWithRoles);
        assertNotNull(token);
        assertTrue(jwtService.isTokenValid(token, "admin"));
    }

    @Test
    void testRefreshTokenDoesNotExpireSoon() {
        String token = jwtService.generateRefreshToken(testUser);
        boolean isValid = jwtService.isTokenValid(token, "testuser");

        assertTrue(isValid);
    }

    @Test
    void testGenerateTokensWithDifferentUsers() {
        User user2 = User.builder()
                .id(2L)
                .username("seconduser")
                .password("password456")
                .roles(Set.of())
                .build();

        String token1 = jwtService.generateAccessToken(testUser);
        String token2 = jwtService.generateAccessToken(user2);

        assertNotEquals(token1, token2);
        assertEquals("testuser", jwtService.extractUsername(token1));
        assertEquals("seconduser", jwtService.extractUsername(token2));
    }
}
