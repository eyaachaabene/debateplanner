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
        studentRole.setName(ERole.STUDENT);

        adminRole = new Role();
        adminRole.setId(2L);
        adminRole.setName(ERole.ADMIN);

        professorRole = new Role();
        professorRole.setId(3L);
        professorRole.setName(ERole.PROFESSOR);
    }

    @Test
    void testFindByNameStudent() {
        when(roleRepository.findByName(ERole.STUDENT)).thenReturn(Optional.of(studentRole));

        Optional<Role> role = roleRepository.findByName(ERole.STUDENT);

        assertTrue(role.isPresent());
        assertEquals(ERole.STUDENT, role.get().getName());
        verify(roleRepository).findByName(ERole.STUDENT);
    }

    @Test
    void testFindByNameAdmin() {
        when(roleRepository.findByName(ERole.ADMIN)).thenReturn(Optional.of(adminRole));

        Optional<Role> role = roleRepository.findByName(ERole.ADMIN);

        assertTrue(role.isPresent());
        assertEquals(ERole.ADMIN, role.get().getName());
    }

    @Test
    void testFindByNameProfessor() {
        when(roleRepository.findByName(ERole.PROFESSOR)).thenReturn(Optional.of(professorRole));

        Optional<Role> role = roleRepository.findByName(ERole.PROFESSOR);

        assertTrue(role.isPresent());
        assertEquals(ERole.PROFESSOR, role.get().getName());
    }

    @Test
    void testFindByNameNotFound() {
        when(roleRepository.findByName(ERole.STUDENT)).thenReturn(Optional.empty());

        Optional<Role> role = roleRepository.findByName(ERole.STUDENT);

        assertFalse(role.isPresent());
    }

    @Test
    void testRoleIdGeneration() {
        assertNotNull(studentRole.getId());
        assertEquals(1L, studentRole.getId());
    }

    @Test
    void testRoleNameSet() {
        assertEquals(ERole.STUDENT, studentRole.getName());
        assertEquals(ERole.ADMIN, adminRole.getName());
        assertEquals(ERole.PROFESSOR, professorRole.getName());
    }

    @Test
    void testAllRolesExist() {
        when(roleRepository.findByName(ERole.STUDENT)).thenReturn(Optional.of(studentRole));
        when(roleRepository.findByName(ERole.ADMIN)).thenReturn(Optional.of(adminRole));
        when(roleRepository.findByName(ERole.PROFESSOR)).thenReturn(Optional.of(professorRole));

        Optional<Role> student = roleRepository.findByName(ERole.STUDENT);
        Optional<Role> admin = roleRepository.findByName(ERole.ADMIN);
        Optional<Role> professor = roleRepository.findByName(ERole.PROFESSOR);

        assertTrue(student.isPresent());
        assertTrue(admin.isPresent());
        assertTrue(professor.isPresent());
    }

    @Test
    void testFindMultipleRoles() {
        when(roleRepository.findByName(ERole.STUDENT)).thenReturn(Optional.of(studentRole));
        when(roleRepository.findByName(ERole.ADMIN)).thenReturn(Optional.of(adminRole));

        Optional<Role> student = roleRepository.findByName(ERole.STUDENT);
        Optional<Role> admin = roleRepository.findByName(ERole.ADMIN);

        assertTrue(student.isPresent());
        assertTrue(admin.isPresent());
        verify(roleRepository, times(2)).findByName(any());
    }
}
