package com.defensemanagement.academic.student.repository;

import com.defensemanagement.academic.student.entity.EMajor;
import com.defensemanagement.academic.student.entity.Student;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StudentRepositoryTest {

    @Mock
    private StudentRepository studentRepository;

    private Student student;

    @BeforeEach
    void setUp() {
        student = Student.builder()
                .id(1L)
                .userId(100L)
                .firstName("John")
                .lastName("Doe")
                .email("john@example.com")
                .major(EMajor.MASTER_SOFTWARE_ENGINEERING)
                .level(3)
                .build();
    }

    @Test
    void testFindByUserIdSuccess() {
        when(studentRepository.findByUserId(100L)).thenReturn(Optional.of(student));

        Optional<Student> result = studentRepository.findByUserId(100L);

        assertTrue(result.isPresent());
        assertEquals(1L, result.get().getId());
        assertEquals(100L, result.get().getUserId());
        verify(studentRepository).findByUserId(100L);
    }

    @Test
    void testFindByUserIdNotFound() {
        when(studentRepository.findByUserId(999L)).thenReturn(Optional.empty());

        Optional<Student> result = studentRepository.findByUserId(999L);

        assertFalse(result.isPresent());
        verify(studentRepository).findByUserId(999L);
    }

    @Test
    void testFindByEmailSuccess() {
        when(studentRepository.findByEmail("john@example.com")).thenReturn(Optional.of(student));

        Optional<Student> result = studentRepository.findByEmail("john@example.com");

        assertTrue(result.isPresent());
        assertEquals("john@example.com", result.get().getEmail());
        verify(studentRepository).findByEmail("john@example.com");
    }

    @Test
    void testFindByEmailNotFound() {
        when(studentRepository.findByEmail("nonexistent@example.com")).thenReturn(Optional.empty());

        Optional<Student> result = studentRepository.findByEmail("nonexistent@example.com");

        assertFalse(result.isPresent());
    }

    @Test
    void testExistsByEmailTrue() {
        when(studentRepository.existsByEmail("john@example.com")).thenReturn(true);

        boolean exists = studentRepository.existsByEmail("john@example.com");

        assertTrue(exists);
        verify(studentRepository).existsByEmail("john@example.com");
    }

    @Test
    void testExistsByEmailFalse() {
        when(studentRepository.existsByEmail("notexist@example.com")).thenReturn(false);

        boolean exists = studentRepository.existsByEmail("notexist@example.com");

        assertFalse(exists);
    }

    @Test
    void testExistsByUserIdTrue() {
        when(studentRepository.existsByUserId(100L)).thenReturn(true);

        boolean exists = studentRepository.existsByUserId(100L);

        assertTrue(exists);
    }

    @Test
    void testExistsByUserIdFalse() {
        when(studentRepository.existsByUserId(999L)).thenReturn(false);

        boolean exists = studentRepository.existsByUserId(999L);

        assertFalse(exists);
    }

    @Test
    void testFindByMajor() {
        List<Student> students = List.of(student);
        when(studentRepository.findByMajor(EMajor.MASTER_SOFTWARE_ENGINEERING)).thenReturn(students);

        List<Student> result = studentRepository.findByMajor(EMajor.MASTER_SOFTWARE_ENGINEERING);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(EMajor.MASTER_SOFTWARE_ENGINEERING, result.get(0).getMajor());
    }

    @Test
    void testFindByLevel() {
        List<Student> students = List.of(student);
        when(studentRepository.findByLevel(3)).thenReturn(students);

        List<Student> result = studentRepository.findByLevel(3);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(3, result.get(0).getLevel());
    }

    @Test
    void testFindByMajorAndLevel() {
        List<Student> students = List.of(student);
        when(studentRepository.findByMajorAndLevel(EMajor.MASTER_SOFTWARE_ENGINEERING, 3))
                .thenReturn(students);

        List<Student> result = studentRepository.findByMajorAndLevel(EMajor.MASTER_SOFTWARE_ENGINEERING, 3);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(EMajor.MASTER_SOFTWARE_ENGINEERING, result.get(0).getMajor());
        assertEquals(3, result.get(0).getLevel());
    }
}
