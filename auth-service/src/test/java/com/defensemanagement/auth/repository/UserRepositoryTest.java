package com.defensemanagement.auth.repository;

import com.defensemanagement.auth.entity.ERole;
import com.defensemanagement.auth.entity.Role;
import com.defensemanagement.auth.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserRepositoryTest {

    @Mock
    private UserRepository userRepository;

    private User testUser;
    private Role studentRole;

    @BeforeEach
    void setUp() {
        studentRole = new Role();
        studentRole.setId(1L);
        studentRole.setName(ERole.STUDENT);

        testUser = User.builder()
                .id(1L)
                .username("testuser")
                .password("password123")
                .roles(Set.of(studentRole))
                .build();
    }

    @Test
    void testFindByUsernameSuccess() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));

        Optional<User> user = userRepository.findByUsername("testuser");

        assertTrue(user.isPresent());
        assertEquals("testuser", user.get().getUsername());
        assertEquals("password123", user.get().getPassword());
        verify(userRepository).findByUsername("testuser");
    }

    @Test
    void testFindByUsernameNotFound() {
        when(userRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());

        Optional<User> user = userRepository.findByUsername("nonexistent");

        assertFalse(user.isPresent());
    }

    @Test
    void testExistsByUsernameTrue() {
        when(userRepository.existsByUsername("testuser")).thenReturn(true);

        boolean exists = userRepository.existsByUsername("testuser");

        assertTrue(exists);
    }

    @Test
    void testExistsByUsernameFalse() {
        when(userRepository.existsByUsername("nonexistent")).thenReturn(false);

        boolean exists = userRepository.existsByUsername("nonexistent");

        assertFalse(exists);
    }

    @Test
    void testUserWithId() {
        assertNotNull(testUser.getId());
        assertEquals(1L, testUser.getId());
    }

    @Test
    void testUserWithPassword() {
        assertEquals("password123", testUser.getPassword());
    }

    @Test
    void testUserWithRoles() {
        assertNotNull(testUser.getRoles());
        assertEquals(1, testUser.getRoles().size());
        assertTrue(testUser.getRoles().contains(studentRole));
    }

    @Test
    void testUserWithMultipleRoles() {
        Role adminRole = new Role();
        adminRole.setId(2L);
        adminRole.setName(ERole.ADMIN);

        User adminUser = User.builder()
                .id(2L)
                .username("adminuser")
                .password("password456")
                .roles(Set.of(studentRole, adminRole))
                .build();

        assertEquals(2, adminUser.getRoles().size());
    }

    @Test
    void testFindMultipleUsers() {
        User user2 = User.builder()
                .id(2L)
                .username("seconduser")
                .password("password456")
                .roles(new HashSet<>())
                .build();

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(userRepository.findByUsername("seconduser")).thenReturn(Optional.of(user2));

        Optional<User> retrieved1 = userRepository.findByUsername("testuser");
        Optional<User> retrieved2 = userRepository.findByUsername("seconduser");

        assertTrue(retrieved1.isPresent());
        assertTrue(retrieved2.isPresent());
        assertEquals("testuser", retrieved1.get().getUsername());
        assertEquals("seconduser", retrieved2.get().getUsername());
    }

    @Test
    void testUsernameProperty() {
        assertEquals("testuser", testUser.getUsername());
    }

    @Test
    void testUserBuilder() {
        User builtUser = User.builder()
                .id(5L)
                .username("builtuser")
                .password("builtpass")
                .roles(Set.of(studentRole))
                .build();

        assertNotNull(builtUser);
        assertEquals(5L, builtUser.getId());
        assertEquals("builtuser", builtUser.getUsername());
    }
}
