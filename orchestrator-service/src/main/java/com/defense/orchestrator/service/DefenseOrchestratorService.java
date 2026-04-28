package com.defense.orchestrator.service;

import com.defense.orchestrator.client.AcademicClient;
import com.defense.orchestrator.client.DefenseClient;
import com.defense.orchestrator.client.RoomClient;
import com.defense.orchestrator.dto.AcademicProfessorResponse;
import com.defense.orchestrator.dto.AcademicStudentResponse;
import com.defense.orchestrator.dto.AvailableProfessorResponse;
import com.defense.orchestrator.dto.DefenseRequest;
import com.defense.orchestrator.dto.DefenseResponse;
import com.defense.orchestrator.dto.GradeRequest;
import com.defense.orchestrator.dto.JuryAssignmentRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DefenseOrchestratorService {

    private final AcademicClient academicClient;
    private final RoomClient roomClient;
    private final DefenseClient defenseClient;

    public Mono<List<DefenseResponse>> getAllDefenses(HttpHeaders headers) {
        return defenseClient.getAllDefenses(headers)
                .collectList();
    }

    public Mono<DefenseResponse> getDefenseById(Long id, HttpHeaders headers) {
        return defenseClient.getById(id, headers);
    }

    public Mono<DefenseResponse> createDefense(DefenseRequest request, HttpHeaders headers) {
        return ensureDefenseEntitiesExist(request, headers)
                .then(defenseClient.createDefense(request, headers));
    }

    public Mono<DefenseResponse> updateDefense(Long id, DefenseRequest request, HttpHeaders headers) {
        return ensureDefenseEntitiesExist(request, headers)
                .then(defenseClient.updateDefense(id, request, headers));
    }

    public Mono<JuryAssignmentRequest> updateJury(Long id, JuryAssignmentRequest request, HttpHeaders headers) {
        return Mono.when(
                        academicClient.getProfessorById(request.getSupervisorId(), headers),
                        academicClient.getProfessorById(request.getPresidentId(), headers),
                        academicClient.getProfessorById(request.getReviewerId(), headers),
                        academicClient.getProfessorById(request.getExaminerId(), headers)
                )
                .then(defenseClient.updateJury(id, request, headers));
    }

    public Mono<DefenseResponse> submitGrade(Long defenseId, String role, GradeRequest request, HttpHeaders headers) {
        return academicClient.getCurrentProfessor(headers)
                .flatMap(professor -> defenseClient.getById(defenseId, headers)
                        .flatMap(defense -> {
                            validateProfessorAuthorization(defense, professor, role);
                            return defenseClient.submitGrade(defenseId, role, request.getGrade(), headers);
                        }));
    }

    public Mono<DefenseResponse> getCurrentStudentResult(HttpHeaders headers) {
        return academicClient.getCurrentStudent(headers)
                .flatMap(student -> defenseClient.getByStudent(student.getId(), headers));
    }

    public Mono<List<DefenseResponse>> getJuryDefenses(HttpHeaders headers, String status) {
        return academicClient.getCurrentProfessor(headers)
                .flatMap(professor -> defenseClient.getAllDefenses(headers)
                        .filter(defense -> isProfessorInJury(defense, professor.getId()))
                        .filter(defense -> matchesJuryStatus(defense, professor.getId(), status))
                        .collectList());
    }

    public Mono<List<AvailableProfessorResponse>> getAvailableJuryMembers(HttpHeaders headers,
                                                                         String role,
                                                                         LocalDate date,
                                                                         LocalTime startTime,
                                                                         LocalTime endTime,
                                                                         Long excludeDefenseId) {
        validateRole(role);
        return Mono.zip(
                        academicClient.getAllProfessors(headers).collectList(),
                        defenseClient.getAllDefenses(headers).collectList()
                )
                .map(tuple -> {
                    List<AcademicProfessorResponse> professors = tuple.getT1();
                    List<DefenseResponse> defenses = tuple.getT2();

                    Set<Long> occupiedProfessorIds = defenses.stream()
                            .filter(defense -> defense.getDefenseDate().equals(date))
                            .filter(defense -> excludeDefenseId == null || !excludeDefenseId.equals(defense.getId()))
                            .filter(defense -> timesOverlap(defense.getStartTime(), defense.getEndTime(), startTime, endTime))
                            .flatMap(defense -> List.of(defense.getSupervisorId(), defense.getPresidentId(), defense.getReviewerId(), defense.getExaminerId()).stream())
                            .collect(Collectors.toSet());

                    return professors.stream()
                            .filter(professor -> !occupiedProfessorIds.contains(professor.getId()))
                            .map(this::toAvailableProfessorResponse)
                            .collect(Collectors.toList());
                });
    }

    private Mono<Void> ensureDefenseEntitiesExist(DefenseRequest request, HttpHeaders headers) {
        return Mono.when(
                academicClient.getStudentById(request.getStudentId(), headers).then(),
                academicClient.getProfessorById(request.getSupervisorId(), headers).then(),
                academicClient.getProfessorById(request.getPresidentId(), headers).then(),
                academicClient.getProfessorById(request.getReviewerId(), headers).then(),
                academicClient.getProfessorById(request.getExaminerId(), headers).then(),
                roomClient.ensureRoomExists(request.getRoomId(), headers)
        );
    }

    private void validateProfessorAuthorization(DefenseResponse defense, AcademicProfessorResponse professor, String role) {
        Long professorId = professor.getId();
        boolean authorized = switch (role.toLowerCase()) {
            case "president" -> professorId.equals(defense.getPresidentId());
            case "reviewer" -> professorId.equals(defense.getReviewerId());
            case "examiner" -> professorId.equals(defense.getExaminerId());
            case "supervisor" -> professorId.equals(defense.getSupervisorId());
            default -> false;
        };

        if (!authorized) {
            throw new IllegalArgumentException("Current professor is not assigned as " + role + " for this defense");
        }
    }

    private boolean isProfessorInJury(DefenseResponse defense, Long professorId) {
        return professorId.equals(defense.getSupervisorId())
                || professorId.equals(defense.getPresidentId())
                || professorId.equals(defense.getReviewerId())
                || professorId.equals(defense.getExaminerId());
    }

    private boolean matchesJuryStatus(DefenseResponse defense, Long professorId, String status) {
        if (status == null || status.isBlank()) {
            return true;
        }
        return switch (status.toUpperCase()) {
            case "UPCOMING" -> "PLANNED".equals(defense.getStatus());
            case "TODO" -> !"PUBLISHED".equals(defense.getStatus()) && missingProfessorGrade(defense, professorId);
            case "DONE" -> "PUBLISHED".equals(defense.getStatus());
            default -> true;
        };
    }

    private boolean missingProfessorGrade(DefenseResponse defense, Long professorId) {
        if (professorId.equals(defense.getSupervisorId())) {
            return defense.getSupervisorGrade() == null;
        }
        if (professorId.equals(defense.getPresidentId())) {
            return defense.getPresidentGrade() == null;
        }
        if (professorId.equals(defense.getReviewerId())) {
            return defense.getReviewerGrade() == null;
        }
        return professorId.equals(defense.getExaminerId()) && defense.getExaminerGrade() == null;
    }

    private void validateRole(String role) {
        if (role == null || role.isBlank()) {
            throw new IllegalArgumentException("Role is required");
        }
        String normalized = role.trim().toUpperCase();
        if (!Set.of("SUPERVISOR", "PRESIDENT", "REVIEWER", "EXAMINER").contains(normalized)) {
            throw new IllegalArgumentException("Unsupported jury role: " + role);
        }
    }

    private boolean timesOverlap(LocalTime existingStart, LocalTime existingEnd, LocalTime newStart, LocalTime newEnd) {
        return existingStart.isBefore(newEnd) && existingEnd.isAfter(newStart);
    }

    private AvailableProfessorResponse toAvailableProfessorResponse(AcademicProfessorResponse professor) {
        AvailableProfessorResponse response = new AvailableProfessorResponse();
        response.setId(professor.getId());
        response.setUserId(professor.getUserId());
        response.setFirstName(professor.getFirstName());
        response.setLastName(professor.getLastName());
        response.setEmail(professor.getEmail());
        return response;
    }
}
