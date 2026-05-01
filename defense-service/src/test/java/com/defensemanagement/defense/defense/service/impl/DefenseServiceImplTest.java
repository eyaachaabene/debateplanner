package com.defensemanagement.defense.defense.service.impl;

import com.defensemanagement.defense.defense.dto.CheckConflictsRequest;
import com.defensemanagement.defense.defense.dto.DefenseRequest;
import com.defensemanagement.defense.defense.dto.DefenseRequestContext;
import com.defensemanagement.defense.defense.dto.DefenseResponse;
import com.defensemanagement.defense.defense.dto.JuryDashboardStatus;
import com.defensemanagement.defense.defense.entity.Defense;
import com.defensemanagement.defense.defense.entity.DefenseStatus;
import com.defensemanagement.defense.defense.entity.Mention;
import com.defensemanagement.defense.defense.mapper.DefenseMapper;
import com.defensemanagement.defense.defense.repository.DefenseRepository;
import com.defensemanagement.defense.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DefenseServiceImplTest {

    @Mock
    private DefenseRepository defenseRepository;

    @Mock
    private DefenseMapper defenseMapper;

    @InjectMocks
    private DefenseServiceImpl defenseService;

    private DefenseRequest defenseRequest;
    private Defense defense;
    private DefenseResponse defenseResponse;
    private DefenseRequestContext requestContext;

    @BeforeEach
    void setUp() {
        defenseRequest = new DefenseRequest();
        defenseRequest.setProjectTitle("AI-based Planning");
        defenseRequest.setDefenseDate(LocalDate.of(2026, 4, 2));
        defenseRequest.setStartTime(LocalTime.of(9, 0));
        defenseRequest.setEndTime(LocalTime.of(10, 0));
        defenseRequest.setStudentId(1L);
        defenseRequest.setSupervisorId(2L);
        defenseRequest.setPresidentId(3L);
        defenseRequest.setReviewerId(4L);
        defenseRequest.setExaminerId(5L);
        defenseRequest.setRoomId(6L);

        defense = Defense.builder()
                .id(10L)
                .projectTitle(defenseRequest.getProjectTitle())
                .defenseDate(defenseRequest.getDefenseDate())
                .startTime(defenseRequest.getStartTime())
                .endTime(defenseRequest.getEndTime())
                .status(DefenseStatus.PLANNED)
                .studentId(defenseRequest.getStudentId())
                .supervisorId(defenseRequest.getSupervisorId())
                .presidentId(defenseRequest.getPresidentId())
                .reviewerId(defenseRequest.getReviewerId())
                .examinerId(defenseRequest.getExaminerId())
                .roomId(defenseRequest.getRoomId())
                .build();

        defenseResponse = DefenseResponse.builder()
                .id(10L)
                .projectTitle(defenseRequest.getProjectTitle())
                .defenseDate(defenseRequest.getDefenseDate())
                .startTime(defenseRequest.getStartTime())
                .endTime(defenseRequest.getEndTime())
                .status(com.defensemanagement.defense.defense.dto.DefenseStatus.PLANNED)
                .studentId(defenseRequest.getStudentId())
                .supervisorId(defenseRequest.getSupervisorId())
                .presidentId(defenseRequest.getPresidentId())
                .reviewerId(defenseRequest.getReviewerId())
                .examinerId(defenseRequest.getExaminerId())
                .roomId(defenseRequest.getRoomId())
                .build();

        requestContext = DefenseRequestContext.builder()
                .userId("7")
                .username("admin")
                .roles("ROLE_ADMIN")
                .requestId("req-1")
                .build();
    }

    @Test
    void testCreateSuccess() {
        when(defenseRepository.findByDefenseDate(defenseRequest.getDefenseDate())).thenReturn(List.of());
        when(defenseMapper.toEntity(defenseRequest)).thenReturn(defense);
        when(defenseRepository.save(defense)).thenReturn(defense);
        when(defenseMapper.toResponse(defense)).thenReturn(defenseResponse);

        DefenseResponse result = defenseService.create(defenseRequest, requestContext);

        assertNotNull(result);
        assertEquals(10L, result.getId());
        assertEquals("AI-based Planning", result.getProjectTitle());
        verify(defenseRepository).save(defense);
    }

    @Test
    void testCreateConflictThrowsIllegalArgumentException() {
        Defense conflictingDefense = Defense.builder()
                .id(99L)
                .defenseDate(defenseRequest.getDefenseDate())
                .startTime(LocalTime.of(9, 30))
                .endTime(LocalTime.of(10, 30))
                .studentId(100L)
                .supervisorId(200L)
                .presidentId(201L)
                .reviewerId(202L)
                .examinerId(203L)
                .roomId(defenseRequest.getRoomId())
                .build();
        when(defenseRepository.findByDefenseDate(defenseRequest.getDefenseDate())).thenReturn(List.of(conflictingDefense));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> defenseService.create(defenseRequest, requestContext));

        assertTrue(exception.getMessage().contains("Room is already occupied"));
        verify(defenseRepository, never()).save(any(Defense.class));
    }

    @Test
    void testGetByIdNotFound() {
        when(defenseRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> defenseService.getById(999L));
    }

    @Test
    void testCheckConflictsReturnsConflicts() {
        CheckConflictsRequest request = new CheckConflictsRequest();
        request.setDefenseDate(defenseRequest.getDefenseDate());
        request.setStartTime(defenseRequest.getStartTime());
        request.setEndTime(defenseRequest.getEndTime());
        request.setStudentId(77L);
        request.setSupervisorId(2L);
        request.setPresidentId(3L);
        request.setReviewerId(4L);
        request.setExaminerId(5L);
        request.setRoomId(66L);

        when(defenseRepository.findByDefenseDate(defenseRequest.getDefenseDate())).thenReturn(List.of(defense));

        var response = defenseService.checkConflicts(request);

        assertTrue(response.isHasConflicts());
        assertFalse(response.getErrors().isEmpty());
    }

    @Test
    void testPublishSuccessComputesAverageAndMention() {
        defense.setSupervisorGrade(14.0);
        defense.setPresidentGrade(16.0);
        defense.setReviewerGrade(15.0);
        defense.setExaminerGrade(17.0);

        Defense publishedDefense = Defense.builder()
                .id(defense.getId())
                .projectTitle(defense.getProjectTitle())
                .defenseDate(defense.getDefenseDate())
                .startTime(defense.getStartTime())
                .endTime(defense.getEndTime())
                .status(DefenseStatus.PUBLISHED)
                .finalAverage(15.5)
                .mention(Mention.GOOD)
                .studentId(defense.getStudentId())
                .supervisorId(defense.getSupervisorId())
                .presidentId(defense.getPresidentId())
                .reviewerId(defense.getReviewerId())
                .examinerId(defense.getExaminerId())
                .roomId(defense.getRoomId())
                .build();

        DefenseResponse publishedResponse = DefenseResponse.builder()
                .id(defense.getId())
                .status(com.defensemanagement.defense.defense.dto.DefenseStatus.PUBLISHED)
                .finalAverage(15.5)
                .mention(Mention.GOOD)
                .build();

        when(defenseRepository.findById(defense.getId())).thenReturn(Optional.of(defense));
        when(defenseRepository.save(defense)).thenReturn(publishedDefense);
        when(defenseMapper.toResponse(publishedDefense)).thenReturn(publishedResponse);

        DefenseResponse result = defenseService.publish(defense.getId());

        assertEquals(com.defensemanagement.defense.defense.dto.DefenseStatus.PUBLISHED, result.getStatus());
        assertEquals(15.5, result.getFinalAverage());
        assertEquals(Mention.GOOD, result.getMention());
    }

    @Test
    void testPublishWithoutAllGradesThrowsIllegalArgumentException() {
        defense.setSupervisorGrade(14.0);
        defense.setPresidentGrade(16.0);
        defense.setReviewerGrade(null);
        defense.setExaminerGrade(17.0);

        when(defenseRepository.findById(defense.getId())).thenReturn(Optional.of(defense));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> defenseService.publish(defense.getId()));

        assertTrue(exception.getMessage().contains("Reviewer grade is required"));
    }

    @Test
    void testSubmitPresidentGradeTransitionsDefenseToOngoing() {
        when(defenseRepository.findById(defense.getId())).thenReturn(Optional.of(defense));
        when(defenseRepository.save(defense)).thenReturn(defense);
        when(defenseMapper.toResponse(defense)).thenReturn(defenseResponse);

        defenseService.submitPresidentGrade(defense.getId(), 18.0);

        assertEquals(18.0, defense.getPresidentGrade());
        assertEquals(DefenseStatus.ONGOING, defense.getStatus());
    }

    @Test
    void testGetJuryDefensesForTodoFiltersMissingGradeForCurrentProfessor() {
        Defense otherDefense = Defense.builder()
                .id(11L)
                .status(DefenseStatus.PUBLISHED)
                .supervisorId(2L)
                .presidentId(8L)
                .reviewerId(9L)
                .examinerId(10L)
                .build();
        defense.setSupervisorGrade(null);

        when(defenseRepository.findAllByOrderByDefenseDateAscStartTimeAsc()).thenReturn(List.of(defense, otherDefense));
        when(defenseMapper.toResponse(defense)).thenReturn(defenseResponse);

        List<DefenseResponse> results = defenseService.getJuryDefenses(requestContext, JuryDashboardStatus.TODO);

        assertEquals(1, results.size());
        assertEquals(defenseResponse, results.getFirst());
    }
}
