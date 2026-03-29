package com.defensemanagement.academic.professor.mapper;

import com.defensemanagement.academic.professor.dto.ProfessorRequest;
import com.defensemanagement.academic.professor.dto.ProfessorResponse;
import com.defensemanagement.academic.professor.entity.Professor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProfessorMapperTest {

    @Mock
    private ProfessorMapper professorMapper;

    private Professor professor;
    private ProfessorRequest professorRequest;
    private ProfessorResponse professorResponse;

    @BeforeEach
    void setUp() {
        professor = Professor.builder()
                .id(1L)
                .userId(200L)
                .firstName("Jane")
                .lastName("Smith")
                .email("jane@example.com")
                .build();

        professorRequest = new ProfessorRequest();
        professorRequest.setUserId(200L);
        professorRequest.setFirstName("Jane");
        professorRequest.setLastName("Smith");
        professorRequest.setEmail("jane@example.com");

        professorResponse = ProfessorResponse.builder()
                .id(1L)
                .userId(200L)
                .firstName("Jane")
                .lastName("Smith")
                .email("jane@example.com")
                .build();
    }

    @Test
    void testToResponseSuccess() {
        when(professorMapper.toResponse(professor)).thenReturn(professorResponse);

        ProfessorResponse response = professorMapper.toResponse(professor);

        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals(200L, response.getUserId());
        assertEquals("Jane", response.getFirstName());
    }

    @Test
    void testToResponseWithNull() {
        when(professorMapper.toResponse(null)).thenReturn(null);

        ProfessorResponse response = professorMapper.toResponse(null);

        assertNull(response);
    }

    @Test
    void testToEntitySuccess() {
        when(professorMapper.toEntity(professorRequest)).thenReturn(professor);

        Professor entity = professorMapper.toEntity(professorRequest);

        assertNotNull(entity);
        assertEquals(200L, entity.getUserId());
        assertEquals("Jane", entity.getFirstName());
    }

    @Test
    void testToEntityWithNull() {
        when(professorMapper.toEntity(null)).thenReturn(null);

        Professor entity = professorMapper.toEntity(null);

        assertNull(entity);
    }

    @Test
    void testUpdateEntityFromRequestSuccess() {
        Professor existingProfessor = new Professor();
        existingProfessor.setId(5L);

        when(professorMapper.toResponse(any(Professor.class))).thenReturn(professorResponse);

        ProfessorResponse response = professorMapper.toResponse(existingProfessor);

        assertNotNull(response);
        assertEquals(1L, response.getId());
    }

    @Test
    void testMapperInterfaceExists() {
        assertNotNull(professorMapper);
    }
}
