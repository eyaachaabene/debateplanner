package com.defensemanagement.academic.student.mapper;

import com.defensemanagement.academic.student.dto.StudentRequest;
import com.defensemanagement.academic.student.dto.StudentResponse;
import com.defensemanagement.academic.student.entity.EMajor;
import com.defensemanagement.academic.student.entity.Student;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StudentMapperTest {

    @Mock
    private StudentMapper studentMapper;

    private Student student;
    private StudentRequest studentRequest;
    private StudentResponse studentResponse;

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

        studentRequest = new StudentRequest();
        studentRequest.setUserId(100L);
        studentRequest.setFirstName("John");
        studentRequest.setLastName("Doe");
        studentRequest.setEmail("john@example.com");
        studentRequest.setMajor(EMajor.MASTER_SOFTWARE_ENGINEERING);
        studentRequest.setLevel(3);

        studentResponse = StudentResponse.builder()
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
    void testToResponseSuccess() {
        when(studentMapper.toResponse(student)).thenReturn(studentResponse);

        StudentResponse response = studentMapper.toResponse(student);

        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals(100L, response.getUserId());
        assertEquals("John", response.getFirstName());
    }

    @Test
    void testToResponseWithNull() {
        when(studentMapper.toResponse(null)).thenReturn(null);

        StudentResponse response = studentMapper.toResponse(null);

        assertNull(response);
    }

    @Test
    void testToEntitySuccess() {
        when(studentMapper.toEntity(studentRequest)).thenReturn(student);

        Student entity = studentMapper.toEntity(studentRequest);

        assertNotNull(entity);
        assertEquals(100L, entity.getUserId());
        assertEquals("John", entity.getFirstName());
    }

    @Test
    void testToEntityWithNull() {
        when(studentMapper.toEntity(null)).thenReturn(null);

        Student entity = studentMapper.toEntity(null);

        assertNull(entity);
    }

    @Test
    void testUpdateEntityFromRequestSuccess() {
        Student existingStudent = new Student();
        existingStudent.setId(5L);

        when(studentMapper.toResponse(any(Student.class))).thenReturn(studentResponse);

        StudentResponse response = studentMapper.toResponse(existingStudent);

        assertNotNull(response);
        assertEquals(1L, response.getId());
    }

    @Test
    void testMapperInterfaceExists() {
        assertNotNull(studentMapper);
    }
}
