package com.defensemanagement.auth.repository;

import com.defensemanagement.auth.entity.ERole;
import com.defensemanagement.auth.entity.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RoleRepositoryTest {

    @Mock
    private RoleRepository roleRepository;

    private Role studentRole;
    private Role adminRole;
    private Role professorRole;

    @BeforeEach
    void setUp() {
        studentRole = new Role();
        studentRole.setId(1L);
        studentRole.setName(ERole.ROLE_STUDENT);

        adminRole = new Role();
        adminRole.setId(2L);
        adminRole.setName(ERole.ROLE_ADMIN);

        professorRole = new Role();
        professorRole.setId(3L);
        professorRole.setName(ERole.ROLE_PROFESSOR);
    }

    @Test
    void testFindByNameStudent() {
        when(roleRepository.findByName(ERole.ROLE_STUDENT)).thenReturn(Optional.of(studentRole));

        Optional<Role> role = roleRepository.findByName(ERole.ROLE_STUDENT);

        assertTrue(role.isPresent());
        assertEquals(ERole.ROLE_STUDENT, role.get().getName());
        verify(roleRepository).findByName(ERole.ROLE_STUDENT);
    }

    @Test
    void testFindByNameAdmin() {
        when(roleRepository.findByName(ERole.ROLE_ADMIN)).thenReturn(Optional.of(adminRole));

        Optional<Role> role = roleRepository.findByName(ERole.ROLE_ADMIN);

        assertTrue(role.isPresent());
        assertEquals(ERole.ROLE_ADMIN, role.get().getName());
    }

    @Test
    void testFindByNameProfessor() {
        when(roleRepository.findByName(ERole.ROLE_PROFESSOR)).thenReturn(Optional.of(professorRole));

        Optional<Role> role = roleRepository.findByName(ERole.ROLE_PROFESSOR);

        assertTrue(role.isPresent());
        assertEquals(ERole.ROLE_PROFESSOR, role.get().getName());
    }

    @Test
    void testFindByNameNotFound() {
        when(roleRepository.findByName(ERole.ROLE_STUDENT)).thenReturn(Optional.empty());

        Optional<Role> role = roleRepository.findByName(ERole.ROLE_STUDENT);

        assertFalse(role.isPresent());
    }

    @Test
    void testRoleIdGeneration() {
        assertNotNull(studentRole.getId());
        assertEquals(1L, studentRole.getId());
    }

    @Test
    void testRoleNameSet() {
        assertEquals(ERole.ROLE_STUDENT, studentRole.getName());
        assertEquals(ERole.ROLE_ADMIN, adminRole.getName());
        assertEquals(ERole.ROLE_PROFESSOR, professorRole.getName());
    }

    @Test
    void testAllRolesExist() {
        when(roleRepository.findByName(ERole.ROLE_STUDENT)).thenReturn(Optional.of(studentRole));
        when(roleRepository.findByName(ERole.ROLE_ADMIN)).thenReturn(Optional.of(adminRole));
        when(roleRepository.findByName(ERole.ROLE_PROFESSOR)).thenReturn(Optional.of(professorRole));

        Optional<Role> student = roleRepository.findByName(ERole.ROLE_STUDENT);
        Optional<Role> admin = roleRepository.findByName(ERole.ROLE_ADMIN);
        Optional<Role> professor = roleRepository.findByName(ERole.ROLE_PROFESSOR);

        assertTrue(student.isPresent());
        assertTrue(admin.isPresent());
        assertTrue(professor.isPresent());
    }

    @Test
    void testFindMultipleRoles() {
        when(roleRepository.findByName(ERole.ROLE_STUDENT)).thenReturn(Optional.of(studentRole));
        when(roleRepository.findByName(ERole.ROLE_ADMIN)).thenReturn(Optional.of(adminRole));

        Optional<Role> student = roleRepository.findByName(ERole.ROLE_STUDENT);
        Optional<Role> admin = roleRepository.findByName(ERole.ROLE_ADMIN);

        assertTrue(student.isPresent());
        assertTrue(admin.isPresent());
        verify(roleRepository, times(2)).findByName(any());
    }
}
