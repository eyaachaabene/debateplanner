package com.defensemanagement.academic.student.service.impl;

import com.defensemanagement.academic.exception.ResourceNotFoundException;
import com.defensemanagement.academic.student.dto.StudentRequest;
import com.defensemanagement.academic.student.dto.StudentResponse;
import com.defensemanagement.academic.student.entity.EMajor;
import com.defensemanagement.academic.student.entity.Student;
import com.defensemanagement.academic.student.mapper.StudentMapper;
import com.defensemanagement.academic.student.repository.StudentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StudentServiceImplTest {

    @Mock
    private StudentRepository studentRepository;

    @Mock
    private StudentMapper studentMapper;

    @InjectMocks
    private StudentServiceImpl studentService;

    private StudentRequest studentRequest;
    private Student student;
    private StudentResponse studentResponse;

    @BeforeEach
    void setUp() {
        studentRequest = new StudentRequest();
        studentRequest.setUserId(1L);
        studentRequest.setFirstName("John");
        studentRequest.setLastName("Doe");
        studentRequest.setEmail("john.doe@example.com");
        studentRequest.setMajor(EMajor.MASTER_SOFTWARE_ENGINEERING);
        studentRequest.setLevel(2);

        student = Student.builder()
                .id(1L)
                .userId(1L)
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@example.com")
                .major(EMajor.MASTER_SOFTWARE_ENGINEERING)
                .level(2)
                .build();

        studentResponse = StudentResponse.builder()
                .id(1L)
                .userId(1L)
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@example.com")
                .major(EMajor.MASTER_SOFTWARE_ENGINEERING)
                .level(2)
                .build();
    }

    @Test
    void testCreateSuccess() {
        when(studentRepository.existsByEmail(studentRequest.getEmail())).thenReturn(false);
        when(studentMapper.toEntity(studentRequest)).thenReturn(student);
        when(studentRepository.save(any(Student.class))).thenReturn(student);
        when(studentMapper.toResponse(student)).thenReturn(studentResponse);

        StudentResponse result = studentService.create(studentRequest);

        assertNotNull(result);
        assertEqual(1L, result.getId());
        assertEqual("john.doe@example.com", result.getEmail());
        verify(studentRepository, times(1)).existsByEmail(studentRequest.getEmail());
        verify(studentMapper, times(1)).toEntity(studentRequest);
        verify(studentRepository, times(1)).save(any(Student.class));
        verify(studentMapper, times(1)).toResponse(student);
    }

    @Test
    void testCreateEmailAlreadyExists() {
        when(studentRepository.existsByEmail(studentRequest.getEmail())).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> studentService.create(studentRequest));
        verify(studentRepository, times(1)).existsByEmail(studentRequest.getEmail());
        verify(studentMapper, never()).toEntity(any());
    }

    @Test
    void testGetByIdSuccess() {
        when(studentRepository.findById(1L)).thenReturn(Optional.of(student));
        when(studentMapper.toResponse(student)).thenReturn(studentResponse);

        StudentResponse result = studentService.getById(1L);

        assertNotNull(result);
        assertEqual(1L, result.getId());
        verify(studentRepository, times(1)).findById(1L);
        verify(studentMapper, times(1)).toResponse(student);
    }

    @Test
    void testGetByIdNotFound() {
        when(studentRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> studentService.getById(999L));
        verify(studentRepository, times(1)).findById(999L);
    }

    @Test
    void testGetByUserIdSuccess() {
        when(studentRepository.findByUserId(1L)).thenReturn(Optional.of(student));
        when(studentMapper.toResponse(student)).thenReturn(studentResponse);

        StudentResponse result = studentService.getByUserId(1L);

        assertNotNull(result);
        assertEqual(1L, result.getUserId());
        verify(studentRepository, times(1)).findByUserId(1L);
    }

    @Test
    void testGetByUserIdNotFound() {
        when(studentRepository.findByUserId(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> studentService.getByUserId(999L));
    }

    @Test
    void testGetAllSuccess() {
        List<Student> students = List.of(student);
        when(studentRepository.findAll()).thenReturn(students);
        when(studentMapper.toResponse(student)).thenReturn(studentResponse);

        List<StudentResponse> results = studentService.getAll();

        assertNotNull(results);
        assertEqual(1, results.size());
        verify(studentRepository, times(1)).findAll();
        verify(studentMapper, times(1)).toResponse(student);
    }

    @Test
    void testGetByMajorSuccess() {
        List<Student> students = List.of(student);
        when(studentRepository.findByMajor(EMajor.MASTER_SOFTWARE_ENGINEERING)).thenReturn(students);
        when(studentMapper.toResponse(student)).thenReturn(studentResponse);

        List<StudentResponse> results = studentService.getByMajor(EMajor.MASTER_SOFTWARE_ENGINEERING);

        assertNotNull(results);
        assertEqual(1, results.size());
        verify(studentRepository, times(1)).findByMajor(EMajor.MASTER_SOFTWARE_ENGINEERING);
    }

    @Test
    void testGetByLevelSuccess() {
        List<Student> students = List.of(student);
        when(studentRepository.findByLevel(2)).thenReturn(students);
        when(studentMapper.toResponse(student)).thenReturn(studentResponse);

        List<StudentResponse> results = studentService.getByLevel(2);

        assertNotNull(results);
        assertEqual(1, results.size());
        verify(studentRepository, times(1)).findByLevel(2);
    }

    @Test
    void testGetByMajorAndLevelSuccess() {
        List<Student> students = List.of(student);
        when(studentRepository.findByMajorAndLevel(EMajor.MASTER_SOFTWARE_ENGINEERING, 2)).thenReturn(students);
        when(studentMapper.toResponse(student)).thenReturn(studentResponse);

        List<StudentResponse> results = studentService.getByMajorAndLevel(EMajor.MASTER_SOFTWARE_ENGINEERING, 2);

        assertNotNull(results);
        assertEqual(1, results.size());
        verify(studentRepository, times(1)).findByMajorAndLevel(EMajor.MASTER_SOFTWARE_ENGINEERING, 2);
    }

    @Test
    void testUpdateSuccess() {
        when(studentRepository.findById(1L)).thenReturn(Optional.of(student));
        doNothing().when(studentMapper).updateEntityFromRequest(studentRequest, student);
        when(studentRepository.save(any(Student.class))).thenReturn(student);
        when(studentMapper.toResponse(student)).thenReturn(studentResponse);

        StudentResponse result = studentService.update(1L, studentRequest);

        assertNotNull(result);
        assertEqual("John", result.getFirstName());
        verify(studentRepository, times(1)).findById(1L);
        verify(studentMapper, times(1)).updateEntityFromRequest(studentRequest, student);
        verify(studentRepository, times(1)).save(any(Student.class));
    }

    @Test
    void testUpdateNotFound() {
        when(studentRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> studentService.update(999L, studentRequest));
        verify(studentRepository, never()).save(any(Student.class));
    }

    @Test
    void testDeleteSuccess() {
        when(studentRepository.findById(1L)).thenReturn(Optional.of(student));
        doNothing().when(studentRepository).delete(student);

        assertDoesNotThrow(() -> studentService.delete(1L));
        verify(studentRepository, times(1)).findById(1L);
        verify(studentRepository, times(1)).delete(student);
    }

    @Test
    void testDeleteNotFound() {
        when(studentRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> studentService.delete(999L));
        verify(studentRepository, never()).delete(any(Student.class));
    }

    private void assertEqual(Object expected, Object actual) {
        assertEquals(expected, actual);
    }
}
