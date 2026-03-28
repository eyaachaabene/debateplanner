package com.defensemanagement.academic.professor.repository;

import com.defensemanagement.academic.professor.entity.Professor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProfessorRepositoryTest {

    @Mock
    private ProfessorRepository professorRepository;

    private Professor professor;

    @BeforeEach
    void setUp() {
        professor = Professor.builder()
                .id(1L)
                .userId(200L)
                .firstName("Jane")
                .lastName("Smith")
                .email("jane@example.com")
                .build();
    }

    @Test
    void testFindByUserIdSuccess() {
        when(professorRepository.findByUserId(200L)).thenReturn(Optional.of(professor));

        Optional<Professor> result = professorRepository.findByUserId(200L);

        assertTrue(result.isPresent());
        assertEquals(1L, result.get().getId());
        assertEquals(200L, result.get().getUserId());
        verify(professorRepository).findByUserId(200L);
    }

    @Test
    void testFindByUserIdNotFound() {
        when(professorRepository.findByUserId(999L)).thenReturn(Optional.empty());

        Optional<Professor> result = professorRepository.findByUserId(999L);

        assertFalse(result.isPresent());
    }

    @Test
    void testFindByEmailSuccess() {
        when(professorRepository.findByEmail("jane@example.com")).thenReturn(Optional.of(professor));

        Optional<Professor> result = professorRepository.findByEmail("jane@example.com");

        assertTrue(result.isPresent());
        assertEquals("jane@example.com", result.get().getEmail());
        verify(professorRepository).findByEmail("jane@example.com");
    }

    @Test
    void testFindByEmailNotFound() {
        when(professorRepository.findByEmail("nonexistent@example.com")).thenReturn(Optional.empty());

        Optional<Professor> result = professorRepository.findByEmail("nonexistent@example.com");

        assertFalse(result.isPresent());
    }

    @Test
    void testExistsByEmailTrue() {
        when(professorRepository.existsByEmail("jane@example.com")).thenReturn(true);

        boolean exists = professorRepository.existsByEmail("jane@example.com");

        assertTrue(exists);
    }

    @Test
    void testExistsByEmailFalse() {
        when(professorRepository.existsByEmail("notexist@example.com")).thenReturn(false);

        boolean exists = professorRepository.existsByEmail("notexist@example.com");

        assertFalse(exists);
    }

    @Test
    void testExistsByUserIdTrue() {
        when(professorRepository.existsByUserId(200L)).thenReturn(true);

        boolean exists = professorRepository.existsByUserId(200L);

        assertTrue(exists);
    }

    @Test
    void testExistsByUserIdFalse() {
        when(professorRepository.existsByUserId(999L)).thenReturn(false);

        boolean exists = professorRepository.existsByUserId(999L);

        assertFalse(exists);
    }
}
