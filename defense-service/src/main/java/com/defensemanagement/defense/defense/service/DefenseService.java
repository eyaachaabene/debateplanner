package com.defensemanagement.defense.defense.service;

import com.defensemanagement.defense.defense.dto.AvailableProfessorResponse;
import com.defensemanagement.defense.defense.dto.CheckConflictsRequest;
import com.defensemanagement.defense.defense.dto.ConflictCheckResponse;
import com.defensemanagement.defense.defense.dto.DefenseGradesResponse;
import com.defensemanagement.defense.defense.dto.DefenseRequest;
import com.defensemanagement.defense.defense.dto.DefenseRequestContext;
import com.defensemanagement.defense.defense.dto.DefenseResponse;
import com.defensemanagement.defense.defense.dto.DefenseStatus;
import com.defensemanagement.defense.defense.dto.JuryAssignmentRequest;
import com.defensemanagement.defense.defense.dto.JuryDashboardStatus;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public interface DefenseService {
    DefenseResponse create(DefenseRequest request, DefenseRequestContext requestContext);

    DefenseResponse getById(Long id);

    List<DefenseResponse> getAll(DefenseStatus status, LocalDate defenseDate);

    DefenseResponse update(Long id, DefenseRequest request, DefenseRequestContext requestContext);

    void delete(Long id);

    ConflictCheckResponse checkConflicts(CheckConflictsRequest request);

    JuryAssignmentRequest getJury(Long id);

    JuryAssignmentRequest updateJury(Long id, JuryAssignmentRequest request, DefenseRequestContext requestContext);

    DefenseGradesResponse getGrades(Long id);

    DefenseResponse submitPresidentGrade(Long id, Double grade);

    DefenseResponse submitReviewerGrade(Long id, Double grade);

    DefenseResponse submitExaminerGrade(Long id, Double grade);

    DefenseResponse submitSupervisorGrade(Long id, Double grade);

    DefenseResponse publish(Long id);

    DefenseResponse getPublishedResult(Long id);

    DefenseResponse getByStudent(Long studentId);

    DefenseResponse getMyResult(DefenseRequestContext requestContext);

    List<DefenseResponse> getJuryDefenses(DefenseRequestContext requestContext, JuryDashboardStatus status);

    List<AvailableProfessorResponse> getAvailableJuryMembers(DefenseRequestContext requestContext, String role, LocalDate date, LocalTime startTime, LocalTime endTime, Long excludeDefenseId);
}
