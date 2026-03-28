package com.defensemanagement.academic.professor.service.impl;

import com.defensemanagement.academic.exception.ResourceNotFoundException;
import com.defensemanagement.academic.professor.dto.ProfessorRequest;
import com.defensemanagement.academic.professor.dto.ProfessorResponse;
import com.defensemanagement.academic.professor.entity.Professor;
import com.defensemanagement.academic.professor.mapper.ProfessorMapper;
import com.defensemanagement.academic.professor.repository.ProfessorRepository;
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
class ProfessorServiceImplTest {

    @Mock
    private ProfessorRepository professorRepository;

    @Mock
    private ProfessorMapper professorMapper;

    @InjectMocks
    private ProfessorServiceImpl professorService;

    private ProfessorRequest professorRequest;
    private Professor professor;
    private ProfessorResponse professorResponse;

    @BeforeEach
    void setUp() {
        professorRequest = new ProfessorRequest();
        professorRequest.setUserId(10L);
        professorRequest.setFirstName("Jane");
        professorRequest.setLastName("Smith");
        professorRequest.setEmail("jane.smith@example.com");

        professor = Professor.builder()
                .id(1L)
                .userId(10L)
                .firstName("Jane")
                .lastName("Smith")
                .email("jane.smith@example.com")
                .build();

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
        when(professorRepository.existsByEmail(professorRequest.getEmail())).thenReturn(false);
        when(professorMapper.toEntity(professorRequest)).thenReturn(professor);
        when(professorRepository.save(any(Professor.class))).thenReturn(professor);
        when(professorMapper.toResponse(professor)).thenReturn(professorResponse);

        ProfessorResponse result = professorService.create(professorRequest);

        assertNotNull(result);
        assertEqual(1L, result.getId());
        assertEqual("jane.smith@example.com", result.getEmail());
        verify(professorRepository, times(1)).existsByEmail(professorRequest.getEmail());
        verify(professorMapper, times(1)).toEntity(professorRequest);
        verify(professorRepository, times(1)).save(any(Professor.class));
        verify(professorMapper, times(1)).toResponse(professor);
    }

    @Test
    void testCreateEmailAlreadyExists() {
        when(professorRepository.existsByEmail(professorRequest.getEmail())).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> professorService.create(professorRequest));
        verify(professorRepository, times(1)).existsByEmail(professorRequest.getEmail());
        verify(professorMapper, never()).toEntity(any());
    }

    @Test
    void testGetByIdSuccess() {
        when(professorRepository.findById(1L)).thenReturn(Optional.of(professor));
        when(professorMapper.toResponse(professor)).thenReturn(professorResponse);

        ProfessorResponse result = professorService.getById(1L);

        assertNotNull(result);
        assertEqual(1L, result.getId());
        verify(professorRepository, times(1)).findById(1L);
    }

    @Test
    void testGetByIdNotFound() {
        when(professorRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> professorService.getById(999L));
        verify(professorRepository, times(1)).findById(999L);
    }

    @Test
    void testGetByUserIdSuccess() {
        when(professorRepository.findByUserId(10L)).thenReturn(Optional.of(professor));
        when(professorMapper.toResponse(professor)).thenReturn(professorResponse);

        ProfessorResponse result = professorService.getByUserId(10L);

        assertNotNull(result);
        assertEqual(10L, result.getUserId());
        verify(professorRepository, times(1)).findByUserId(10L);
    }

    @Test
    void testGetByUserIdNotFound() {
        when(professorRepository.findByUserId(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> professorService.getByUserId(999L));
    }

    @Test
    void testGetAllSuccess() {
        List<Professor> professors = List.of(professor);
        when(professorRepository.findAll()).thenReturn(professors);
        when(professorMapper.toResponse(professor)).thenReturn(professorResponse);

        List<ProfessorResponse> results = professorService.getAll();

        assertNotNull(results);
        assertEqual(1, results.size());
        verify(professorRepository, times(1)).findAll();
    }

    @Test
    void testUpdateSuccess() {
        when(professorRepository.findById(1L)).thenReturn(Optional.of(professor));
        doNothing().when(professorMapper).updateEntityFromRequest(professorRequest, professor);
        when(professorRepository.save(any(Professor.class))).thenReturn(professor);
        when(professorMapper.toResponse(professor)).thenReturn(professorResponse);

        ProfessorResponse result = professorService.update(1L, professorRequest);

        assertNotNull(result);
        assertEqual("Jane", result.getFirstName());
        verify(professorRepository, times(1)).findById(1L);
        verify(professorMapper, times(1)).updateEntityFromRequest(professorRequest, professor);
        verify(professorRepository, times(1)).save(any(Professor.class));
    }

    @Test
    void testUpdateNotFound() {
        when(professorRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> professorService.update(999L, professorRequest));
        verify(professorRepository, never()).save(any(Professor.class));
    }

    @Test
    void testDeleteSuccess() {
        when(professorRepository.findById(1L)).thenReturn(Optional.of(professor));
        doNothing().when(professorRepository).delete(professor);

        assertDoesNotThrow(() -> professorService.delete(1L));
        verify(professorRepository, times(1)).findById(1L);
        verify(professorRepository, times(1)).delete(professor);
    }

    @Test
    void testDeleteNotFound() {
        when(professorRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> professorService.delete(999L));
        verify(professorRepository, never()).delete(any(Professor.class));
    }

    private void assertEqual(Object expected, Object actual) {
        assertEquals(expected, actual);
    }
}
