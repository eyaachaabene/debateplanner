package com.defensemanagement.defense.defense.controller;

import com.defensemanagement.defense.defense.dto.DefenseRequest;
import com.defensemanagement.defense.defense.dto.DefenseRequestContext;
import com.defensemanagement.defense.defense.dto.DefenseResponse;
import com.defensemanagement.defense.defense.dto.JuryAssignmentRequest;
import com.defensemanagement.defense.defense.service.DefenseService;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DefenseControllerTest {

    @Mock
    private DefenseService defenseService;

    @Mock
    private HttpServletRequest httpServletRequest;

    @InjectMocks
    private DefenseController defenseController;

    private DefenseRequest defenseRequest;
    private DefenseResponse defenseResponse;

    @BeforeEach
    void setUp() {
        defenseRequest = new DefenseRequest();
        defenseRequest.setProjectTitle("Cloud Platform");
        defenseRequest.setDefenseDate(LocalDate.of(2026, 4, 5));
        defenseRequest.setStartTime(LocalTime.of(11, 0));
        defenseRequest.setEndTime(LocalTime.of(12, 0));
        defenseRequest.setStudentId(1L);
        defenseRequest.setSupervisorId(2L);
        defenseRequest.setPresidentId(3L);
        defenseRequest.setReviewerId(4L);
        defenseRequest.setExaminerId(5L);
        defenseRequest.setRoomId(6L);

        defenseResponse = DefenseResponse.builder()
                .id(1L)
                .projectTitle("Cloud Platform")
                .status(com.defensemanagement.defense.defense.dto.DefenseStatus.PLANNED)
                .build();

    }

    @Test
    void testCreateSuccess() {
        when(httpServletRequest.getHeader("X-User-Id")).thenReturn("1");
        when(httpServletRequest.getHeader("X-User-Username")).thenReturn("admin");
        when(httpServletRequest.getHeader("X-User-Roles")).thenReturn("ROLE_ADMIN");
        when(httpServletRequest.getHeader("X-Request-Id")).thenReturn("req-123");
        when(defenseService.create(any(DefenseRequest.class), any(DefenseRequestContext.class)))
                .thenReturn(defenseResponse);

        ResponseEntity<DefenseResponse> response = defenseController.create(httpServletRequest, defenseRequest);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1L, response.getBody().getId());
        verify(defenseService).create(any(DefenseRequest.class), any(DefenseRequestContext.class));
    }

    @Test
    void testGetByIdSuccess() {
        when(defenseService.getById(1L)).thenReturn(defenseResponse);

        ResponseEntity<DefenseResponse> response = defenseController.getById(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Cloud Platform", response.getBody().getProjectTitle());
    }

    @Test
    void testUpdateJurySuccess() {
        when(httpServletRequest.getHeader("X-User-Id")).thenReturn("1");
        when(httpServletRequest.getHeader("X-User-Username")).thenReturn("admin");
        when(httpServletRequest.getHeader("X-User-Roles")).thenReturn("ROLE_ADMIN");
        when(httpServletRequest.getHeader("X-Request-Id")).thenReturn("req-123");
        JuryAssignmentRequest request = new JuryAssignmentRequest();
        request.setSupervisorId(2L);
        request.setPresidentId(3L);
        request.setReviewerId(4L);
        request.setExaminerId(5L);

        when(defenseService.updateJury(any(Long.class), any(JuryAssignmentRequest.class), any(DefenseRequestContext.class)))
                .thenReturn(request);

        ResponseEntity<JuryAssignmentRequest> response = defenseController.updateJury(httpServletRequest, 1L, request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2L, response.getBody().getSupervisorId());
    }

    @Test
    void testDeleteSuccess() {
        ResponseEntity<Void> response = defenseController.delete(1L);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(defenseService).delete(1L);
    }
}
