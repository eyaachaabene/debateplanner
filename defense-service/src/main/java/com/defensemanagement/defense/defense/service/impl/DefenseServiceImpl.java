package com.defensemanagement.defense.defense.service.impl;

import com.defensemanagement.defense.defense.dto.AvailableProfessorResponse;
import com.defensemanagement.defense.defense.dto.CheckConflictsRequest;
import com.defensemanagement.defense.defense.dto.ConflictCheckResponse;
import com.defensemanagement.defense.defense.dto.DefenseGradesResponse;
import com.defensemanagement.defense.defense.dto.DefenseRequest;
import com.defensemanagement.defense.defense.dto.DefenseRequestContext;
import com.defensemanagement.defense.defense.dto.DefenseResponse;
import com.defensemanagement.defense.defense.dto.JuryAssignmentRequest;
import com.defensemanagement.defense.defense.dto.JuryDashboardStatus;
import com.defensemanagement.defense.defense.entity.Defense;
import com.defensemanagement.defense.defense.entity.DefenseStatus;
import com.defensemanagement.defense.defense.entity.Mention;
import com.defensemanagement.defense.defense.mapper.DefenseMapper;
import com.defensemanagement.defense.defense.repository.DefenseRepository;
import com.defensemanagement.defense.defense.service.DefenseService;
import com.defensemanagement.defense.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class DefenseServiceImpl implements DefenseService {
    private final DefenseRepository defenseRepository;
    private final DefenseMapper defenseMapper;

    @Override
    public DefenseResponse create(DefenseRequest request, DefenseRequestContext requestContext) {
        validateDefenseRequest(request, null);
        return defenseMapper.toResponse(defenseRepository.save(defenseMapper.toEntity(request)));
    }

    @Override
    @Transactional(readOnly = true)
    public DefenseResponse getById(Long id) {
        return defenseMapper.toResponse(getDefenseEntity(id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<DefenseResponse> getAll(com.defensemanagement.defense.defense.dto.DefenseStatus status, LocalDate defenseDate) {
        List<Defense> defenses;

        if (status != null && defenseDate != null) {
            defenses = defenseRepository.findByStatusAndDefenseDateOrderByStartTimeAsc(DefenseStatus.valueOf(status.name()), defenseDate);
        } else if (status != null) {
            defenses = defenseRepository.findByStatusOrderByDefenseDateAscStartTimeAsc(DefenseStatus.valueOf(status.name()));
        } else if (defenseDate != null) {
            defenses = defenseRepository.findByDefenseDateOrderByStartTimeAsc(defenseDate);
        } else {
            defenses = defenseRepository.findAllByOrderByDefenseDateAscStartTimeAsc();
        }

        return defenses.stream().map(defenseMapper::toResponse).toList();
    }

    @Override
    public DefenseResponse update(Long id, DefenseRequest request, DefenseRequestContext requestContext) {
        Defense defense = getDefenseEntity(id);
        if (defense.getStatus() == DefenseStatus.PUBLISHED) {
            throw new IllegalArgumentException("Published defense cannot be updated");
        }

        validateDefenseRequest(request, id);
        defenseMapper.updateEntityFromRequest(request, defense);
        return defenseMapper.toResponse(defenseRepository.save(defense));
    }

    @Override
    public void delete(Long id) {
        Defense defense = getDefenseEntity(id);
        if (defense.getStatus() == DefenseStatus.PUBLISHED) {
            throw new IllegalArgumentException("Published defense cannot be deleted");
        }
        defenseRepository.delete(defense);
    }

    @Override
    @Transactional(readOnly = true)
    public ConflictCheckResponse checkConflicts(CheckConflictsRequest request) {
        validateTimeRange(request.getStartTime(), request.getEndTime());
        List<String> conflicts = detectConflicts(request);
        return ConflictCheckResponse.builder()
                .hasConflicts(!conflicts.isEmpty())
                .errors(conflicts)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public JuryAssignmentRequest getJury(Long id) {
        Defense defense = getDefenseEntity(id);
        JuryAssignmentRequest response = new JuryAssignmentRequest();
        response.setSupervisorId(defense.getSupervisorId());
        response.setPresidentId(defense.getPresidentId());
        response.setReviewerId(defense.getReviewerId());
        response.setExaminerId(defense.getExaminerId());
        return response;
    }

    @Override
    public JuryAssignmentRequest updateJury(Long id, JuryAssignmentRequest request, DefenseRequestContext requestContext) {
        Defense defense = getDefenseEntity(id);
        if (defense.getStatus() == DefenseStatus.PUBLISHED) {
            throw new IllegalArgumentException("Published defense jury cannot be updated");
        }

        validateJuryMembers(request.getSupervisorId(), request.getPresidentId(), request.getReviewerId(), request.getExaminerId());

        CheckConflictsRequest conflictRequest = new CheckConflictsRequest();
        conflictRequest.setDefenseDate(defense.getDefenseDate());
        conflictRequest.setStartTime(defense.getStartTime());
        conflictRequest.setEndTime(defense.getEndTime());
        conflictRequest.setStudentId(defense.getStudentId());
        conflictRequest.setRoomId(defense.getRoomId());
        conflictRequest.setSupervisorId(request.getSupervisorId());
        conflictRequest.setPresidentId(request.getPresidentId());
        conflictRequest.setReviewerId(request.getReviewerId());
        conflictRequest.setExaminerId(request.getExaminerId());
        conflictRequest.setExcludeDefenseId(defense.getId());

        List<String> conflicts = detectConflicts(conflictRequest);
        if (!conflicts.isEmpty()) {
            throw new IllegalArgumentException(String.join(", ", conflicts));
        }

        defense.setSupervisorId(request.getSupervisorId());
        defense.setPresidentId(request.getPresidentId());
        defense.setReviewerId(request.getReviewerId());
        defense.setExaminerId(request.getExaminerId());
        defenseRepository.save(defense);
        return request;
    }

    @Override
    @Transactional(readOnly = true)
    public DefenseGradesResponse getGrades(Long id) {
        Defense defense = getDefenseEntity(id);
        return DefenseGradesResponse.builder()
                .presidentGrade(defense.getPresidentGrade())
                .reviewerGrade(defense.getReviewerGrade())
                .examinerGrade(defense.getExaminerGrade())
                .supervisorGrade(defense.getSupervisorGrade())
                .finalAverage(defense.getFinalAverage())
                .mention(defense.getMention())
                .build();
    }

    @Override
    public DefenseResponse submitPresidentGrade(Long id, Double grade) {
        return updateGrade(id, grade, "president");
    }

    @Override
    public DefenseResponse submitReviewerGrade(Long id, Double grade) {
        return updateGrade(id, grade, "reviewer");
    }

    @Override
    public DefenseResponse submitExaminerGrade(Long id, Double grade) {
        return updateGrade(id, grade, "examiner");
    }

    @Override
    public DefenseResponse submitSupervisorGrade(Long id, Double grade) {
        return updateGrade(id, grade, "supervisor");
    }

    @Override
    public DefenseResponse publish(Long id) {
        Defense defense = getDefenseEntity(id);

        List<Double> grades = List.of(
                requireGrade(defense.getSupervisorGrade(), "Supervisor"),
                requireGrade(defense.getPresidentGrade(), "President"),
                requireGrade(defense.getReviewerGrade(), "Reviewer"),
                requireGrade(defense.getExaminerGrade(), "Examiner")
        );

        double average = Math.round((grades.stream().mapToDouble(Double::doubleValue).average().orElse(0D)) * 100.0) / 100.0;
        defense.setFinalAverage(average);
        defense.setMention(resolveMention(average));
        defense.setStatus(DefenseStatus.PUBLISHED);

        return defenseMapper.toResponse(defenseRepository.save(defense));
    }

    @Override
    @Transactional(readOnly = true)
    public DefenseResponse getPublishedResult(Long id) {
        Defense defense = getDefenseEntity(id);
        if (defense.getStatus() != DefenseStatus.PUBLISHED) {
            throw new ResourceNotFoundException("Result not available for defense with id: " + id);
        }
        return defenseMapper.toResponse(defense);
    }

    @Override
    @Transactional(readOnly = true)
    public DefenseResponse getByStudent(Long studentId) {
        Defense defense = defenseRepository.findTopByStudentIdOrderByDefenseDateDescStartTimeDesc(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Defense not found for student with id: " + studentId));
        return defenseMapper.toResponse(defense);
    }

    @Override
    @Transactional(readOnly = true)
    public DefenseResponse getMyResult(DefenseRequestContext requestContext) {
        throw new UnsupportedOperationException("Current student result should be resolved by the orchestrator using student identity mapping.");
    }

    @Override
    @Transactional(readOnly = true)
    public List<DefenseResponse> getJuryDefenses(DefenseRequestContext requestContext, JuryDashboardStatus status) {
        throw new UnsupportedOperationException("Jury defense listing should be resolved by the orchestrator with professor identity mapping.");
    }

    @Override
    @Transactional(readOnly = true)
    public List<AvailableProfessorResponse> getAvailableJuryMembers(DefenseRequestContext requestContext, String role, LocalDate date, LocalTime startTime, LocalTime endTime, Long excludeDefenseId) {
        throw new UnsupportedOperationException("Available jury members should be resolved by the orchestrator using academic-service professor data.");
    }

    private Defense getDefenseEntity(Long id) {
        return defenseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Defense not found with id: " + id));
    }

    private void validateDefenseRequest(DefenseRequest request, Long excludeDefenseId) {
        validateTimeRange(request.getStartTime(), request.getEndTime());
        validateJuryMembers(request.getSupervisorId(), request.getPresidentId(), request.getReviewerId(), request.getExaminerId());

        CheckConflictsRequest conflictRequest = new CheckConflictsRequest();
        conflictRequest.setDefenseDate(request.getDefenseDate());
        conflictRequest.setStartTime(request.getStartTime());
        conflictRequest.setEndTime(request.getEndTime());
        conflictRequest.setStudentId(request.getStudentId());
        conflictRequest.setSupervisorId(request.getSupervisorId());
        conflictRequest.setPresidentId(request.getPresidentId());
        conflictRequest.setReviewerId(request.getReviewerId());
        conflictRequest.setExaminerId(request.getExaminerId());
        conflictRequest.setRoomId(request.getRoomId());
        conflictRequest.setExcludeDefenseId(excludeDefenseId);

        List<String> conflicts = detectConflicts(conflictRequest);
        if (!conflicts.isEmpty()) {
            throw new IllegalArgumentException(String.join(", ", conflicts));
        }
    }

    private void validateTimeRange(LocalTime startTime, LocalTime endTime) {
        if (startTime == null || endTime == null || !startTime.isBefore(endTime)) {
            throw new IllegalArgumentException("Start time must be before end time");
        }
    }

    private void validateJuryMembers(Long supervisorId, Long presidentId, Long reviewerId, Long examinerId) {
        Set<Long> uniqueIds = new LinkedHashSet<>(List.of(supervisorId, presidentId, reviewerId, examinerId));
        if (uniqueIds.size() < 4) {
            throw new IllegalArgumentException("Each jury member must be different");
        }
    }

    private List<String> detectConflicts(CheckConflictsRequest request) {
        List<String> conflicts = new ArrayList<>();
        List<Defense> sameDayDefenses = defenseRepository.findByDefenseDate(request.getDefenseDate()).stream()
                .filter(defense -> request.getExcludeDefenseId() == null || !request.getExcludeDefenseId().equals(defense.getId()))
                .filter(defense -> timesOverlap(defense.getStartTime(), defense.getEndTime(), request.getStartTime(), request.getEndTime()))
                .toList();

        if (sameDayDefenses.stream().anyMatch(defense -> defense.getRoomId().equals(request.getRoomId()))) {
            conflicts.add("Room is already occupied for the selected slot");
        }
        if (sameDayDefenses.stream().anyMatch(defense -> defense.getStudentId().equals(request.getStudentId()))) {
            conflicts.add("Student already has a defense for the selected slot");
        }
        if (sameDayDefenses.stream().anyMatch(defense -> professorInDefense(defense, request.getSupervisorId()))) {
            conflicts.add("Supervisor is already assigned for the selected slot");
        }
        if (sameDayDefenses.stream().anyMatch(defense -> professorInDefense(defense, request.getPresidentId()))) {
            conflicts.add("President is already assigned for the selected slot");
        }
        if (sameDayDefenses.stream().anyMatch(defense -> professorInDefense(defense, request.getReviewerId()))) {
            conflicts.add("Reviewer is already assigned for the selected slot");
        }
        if (sameDayDefenses.stream().anyMatch(defense -> professorInDefense(defense, request.getExaminerId()))) {
            conflicts.add("Examiner is already assigned for the selected slot");
        }

        return conflicts;
    }

    private boolean professorInDefense(Defense defense, Long professorId) {
        return defense.getSupervisorId().equals(professorId)
                || defense.getPresidentId().equals(professorId)
                || defense.getReviewerId().equals(professorId)
                || defense.getExaminerId().equals(professorId);
    }

    private boolean timesOverlap(LocalTime existingStart, LocalTime existingEnd, LocalTime newStart, LocalTime newEnd) {
        return existingStart.isBefore(newEnd) && existingEnd.isAfter(newStart);
    }

    private DefenseResponse updateGrade(Long id, Double grade, String role) {
        if (grade == null || grade < 0 || grade > 20) {
            throw new IllegalArgumentException("Grade must be between 0 and 20");
        }

        Defense defense = getDefenseEntity(id);
        if (defense.getStatus() == DefenseStatus.PUBLISHED) {
            throw new IllegalArgumentException("Published defense cannot be graded");
        }

        switch (role) {
            case "president" -> defense.setPresidentGrade(grade);
            case "reviewer" -> defense.setReviewerGrade(grade);
            case "examiner" -> defense.setExaminerGrade(grade);
            case "supervisor" -> defense.setSupervisorGrade(grade);
            default -> throw new IllegalArgumentException("Unsupported jury role");
        }

        if (defense.getStatus() == DefenseStatus.PLANNED) {
            defense.setStatus(DefenseStatus.ONGOING);
        }

        return defenseMapper.toResponse(defenseRepository.save(defense));
    }

    private Double requireGrade(Double grade, String role) {
        if (grade == null) {
            throw new IllegalArgumentException(role + " grade is required before publication");
        }
        return grade;
    }

    private Mention resolveMention(double average) {
        if (average < 10) {
            return Mention.FAIL;
        }
        if (average < 12) {
            return Mention.PASSABLE;
        }
        if (average < 14) {
            return Mention.FAIRLY_GOOD;
        }
        if (average < 16) {
            return Mention.GOOD;
        }
        if (average < 18) {
            return Mention.VERY_GOOD;
        }
        return Mention.EXCELLENT;
    }

    private boolean isProfessorInJury(Defense defense, Long professorId) {
        return defense.getSupervisorId().equals(professorId)
                || defense.getPresidentId().equals(professorId)
                || defense.getReviewerId().equals(professorId)
                || defense.getExaminerId().equals(professorId);
    }

    private boolean matchesJuryStatus(Defense defense, Long professorId, JuryDashboardStatus status) {
        if (status == null) {
            return true;
        }

        return switch (status) {
            case UPCOMING -> defense.getStatus() == DefenseStatus.PLANNED;
            case TODO -> defense.getStatus() != DefenseStatus.PUBLISHED && missingProfessorGrade(defense, professorId);
            case DONE -> defense.getStatus() == DefenseStatus.PUBLISHED;
        };
    }

    private boolean missingProfessorGrade(Defense defense, Long professorId) {
        if (defense.getSupervisorId().equals(professorId)) {
            return defense.getSupervisorGrade() == null;
        }
        if (defense.getPresidentId().equals(professorId)) {
            return defense.getPresidentGrade() == null;
        }
        if (defense.getReviewerId().equals(professorId)) {
            return defense.getReviewerGrade() == null;
        }
        return defense.getExaminerId().equals(professorId) && defense.getExaminerGrade() == null;
    }
}
