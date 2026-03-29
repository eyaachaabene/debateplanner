package com.defensemanagement.academic.professor.controller;

import com.defensemanagement.academic.professor.dto.ProfessorRequest;
import com.defensemanagement.academic.professor.dto.ProfessorResponse;
import com.defensemanagement.academic.professor.service.ProfessorService;
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
class ProfessorControllerTest {

    @Mock
    private ProfessorService professorService;

    @InjectMocks
    private ProfessorController professorController;

    private ProfessorRequest professorRequest;
    private ProfessorResponse professorResponse;

    @BeforeEach
    void setUp() {
        professorRequest = new ProfessorRequest();
        professorRequest.setUserId(10L);
        professorRequest.setFirstName("Jane");
        professorRequest.setLastName("Smith");
        professorRequest.setEmail("jane.smith@example.com");

        professorResponse = ProfessorResponse.builder()
                .id(1L)
                .userId(10L)
                .firstName("Jane")
                .lastName("Smith")
                .email("jane.smith@example.com")
                .build();
    }

    @Test
    void testCreateSuccess() {
        when(professorService.create(any(ProfessorRequest.class))).thenReturn(professorResponse);

        ResponseEntity<ProfessorResponse> response = professorController.create(professorRequest);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1L, response.getBody().getId());
        assertEquals("jane.smith@example.com", response.getBody().getEmail());
        verify(professorService, times(1)).create(any(ProfessorRequest.class));
    }

    @Test
    void testGetByIdSuccess() {
        when(professorService.getById(1L)).thenReturn(professorResponse);

        ResponseEntity<ProfessorResponse> response = professorController.getById(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1L, response.getBody().getId());
        assertEquals("Jane", response.getBody().getFirstName());
        verify(professorService, times(1)).getById(1L);
    }

    @Test
    void testGetByUserIdSuccess() {
        when(professorService.getByUserId(10L)).thenReturn(professorResponse);

        ResponseEntity<ProfessorResponse> response = professorController.getByUserId(10L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(10L, response.getBody().getUserId());
        verify(professorService, times(1)).getByUserId(10L);
    }

    @Test
    void testGetAllSuccess() {
        List<ProfessorResponse> responses = List.of(professorResponse);
        when(professorService.getAll()).thenReturn(responses);

        ResponseEntity<List<ProfessorResponse>> response = professorController.getAll();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        assertEquals(1L, response.getBody().get(0).getId());
        verify(professorService, times(1)).getAll();
    }

    @Test
    void testUpdateSuccess() {
        when(professorService.update(eq(1L), any(ProfessorRequest.class)))
                .thenReturn(professorResponse);

        ResponseEntity<ProfessorResponse> response = professorController.update(1L, professorRequest);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1L, response.getBody().getId());
        verify(professorService, times(1)).update(eq(1L), any(ProfessorRequest.class));
    }

    @Test
    void testDeleteSuccess() {
        doNothing().when(professorService).delete(1L);

        ResponseEntity<Void> response = professorController.delete(1L);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(professorService, times(1)).delete(1L);
    }
}
