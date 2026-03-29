package com.defensemanagement.academic.student.controller;

import com.defensemanagement.academic.student.dto.StudentRequest;
import com.defensemanagement.academic.student.dto.StudentResponse;
import com.defensemanagement.academic.student.entity.EMajor;
import com.defensemanagement.academic.student.service.StudentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StudentControllerTest {

    @Mock
    private StudentService studentService;

    @InjectMocks
    private StudentController studentController;

    private StudentRequest studentRequest;
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
        when(studentService.create(any(StudentRequest.class))).thenReturn(studentResponse);

        ResponseEntity<StudentResponse> response = studentController.create(studentRequest);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1L, response.getBody().getId());
        assertEquals("john.doe@example.com", response.getBody().getEmail());
        verify(studentService, times(1)).create(any(StudentRequest.class));
    }

    @Test
    void testGetByIdSuccess() {
        when(studentService.getById(1L)).thenReturn(studentResponse);

        ResponseEntity<StudentResponse> response = studentController.getById(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1L, response.getBody().getId());
        assertEquals("John", response.getBody().getFirstName());
        verify(studentService, times(1)).getById(1L);
    }

    @Test
    void testGetByUserIdSuccess() {
        when(studentService.getByUserId(1L)).thenReturn(studentResponse);

        ResponseEntity<StudentResponse> response = studentController.getByUserId(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1L, response.getBody().getUserId());
        verify(studentService, times(1)).getByUserId(1L);
    }

    @Test
    void testGetAllSuccess() {
        List<StudentResponse> responses = List.of(studentResponse);
        when(studentService.getAll()).thenReturn(responses);

        ResponseEntity<List<StudentResponse>> response = studentController.getAll(null, null);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        assertEquals(1L, response.getBody().get(0).getId());
        verify(studentService, times(1)).getAll();
    }

    @Test
    void testGetByMajor() {
        List<StudentResponse> responses = List.of(studentResponse);
        when(studentService.getByMajor(EMajor.MASTER_SOFTWARE_ENGINEERING))
                .thenReturn(responses);

        ResponseEntity<List<StudentResponse>> response = studentController.getAll(EMajor.MASTER_SOFTWARE_ENGINEERING, null);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        verify(studentService, times(1)).getByMajor(EMajor.MASTER_SOFTWARE_ENGINEERING);
    }

    @Test
    void testGetByLevel() {
        List<StudentResponse> responses = List.of(studentResponse);
        when(studentService.getByLevel(2)).thenReturn(responses);

        ResponseEntity<List<StudentResponse>> response = studentController.getAll(null, 2);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        verify(studentService, times(1)).getByLevel(2);
    }

    @Test
    void testGetByMajorAndLevel() {
        List<StudentResponse> responses = List.of(studentResponse);
        when(studentService.getByMajorAndLevel(EMajor.MASTER_SOFTWARE_ENGINEERING, 2))
                .thenReturn(responses);

        ResponseEntity<List<StudentResponse>> response = studentController.getAll(EMajor.MASTER_SOFTWARE_ENGINEERING, 2);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        verify(studentService, times(1)).getByMajorAndLevel(EMajor.MASTER_SOFTWARE_ENGINEERING, 2);
    }

    @Test
    void testUpdateSuccess() {
        when(studentService.update(eq(1L), any(StudentRequest.class))).thenReturn(studentResponse);

        ResponseEntity<StudentResponse> response = studentController.update(1L, studentRequest);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1L, response.getBody().getId());
        verify(studentService, times(1)).update(eq(1L), any(StudentRequest.class));
    }

    @Test
    void testDeleteSuccess() {
        doNothing().when(studentService).delete(1L);

        ResponseEntity<Void> response = studentController.delete(1L);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(studentService, times(1)).delete(1L);
    }
}
