package com.defensemanagement.defense.defense.service;

import com.defensemanagement.defense.client.AcademicClient;
import com.defensemanagement.defense.defense.dto.AvailableProfessorResponse;
import com.defensemanagement.defense.defense.dto.CheckConflictsRequest;
import com.defensemanagement.defense.defense.dto.ConflictCheckResponse;
import com.defensemanagement.defense.defense.dto.DefenseGradesResponse;
import com.defensemanagement.defense.defense.dto.DefenseRequest;
import com.defensemanagement.defense.defense.dto.DefenseResponse;
import com.defensemanagement.defense.defense.dto.DefenseStatus;
import com.defensemanagement.defense.defense.dto.JuryAssignmentRequest;
import com.defensemanagement.defense.defense.dto.JuryDashboardStatus;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public interface DefenseService {
    DefenseResponse create(DefenseRequest request, AcademicClient.RequestContext requestContext);

    DefenseResponse getById(Long id);

    List<DefenseResponse> getAll(DefenseStatus status, LocalDate defenseDate);

    DefenseResponse update(Long id, DefenseRequest request, AcademicClient.RequestContext requestContext);

    void delete(Long id);

    ConflictCheckResponse checkConflicts(CheckConflictsRequest request);

    JuryAssignmentRequest getJury(Long id);

    JuryAssignmentRequest updateJury(Long id, JuryAssignmentRequest request, AcademicClient.RequestContext requestContext);

    DefenseGradesResponse getGrades(Long id);

    DefenseResponse submitPresidentGrade(Long id, Double grade);

    DefenseResponse submitReviewerGrade(Long id, Double grade);

    DefenseResponse submitExaminerGrade(Long id, Double grade);

    DefenseResponse submitSupervisorGrade(Long id, Double grade);

    DefenseResponse publish(Long id);

    DefenseResponse getPublishedResult(Long id);

    DefenseResponse getByStudent(Long studentId);

    DefenseResponse getMyResult(AcademicClient.RequestContext requestContext);

    List<DefenseResponse> getJuryDefenses(AcademicClient.RequestContext requestContext, JuryDashboardStatus status);

    List<AvailableProfessorResponse> getAvailableJuryMembers(AcademicClient.RequestContext requestContext, String role, LocalDate date, LocalTime startTime, LocalTime endTime, Long excludeDefenseId);
}
