package com.defensemanagement.defense.defense.controller;

import com.defensemanagement.defense.client.AcademicClient;
import com.defensemanagement.defense.defense.dto.AvailableProfessorResponse;
import com.defensemanagement.defense.defense.dto.CheckConflictsRequest;
import com.defensemanagement.defense.defense.dto.ConflictCheckResponse;
import com.defensemanagement.defense.defense.dto.DefenseGradesResponse;
import com.defensemanagement.defense.defense.dto.DefenseRequest;
import com.defensemanagement.defense.defense.dto.DefenseResponse;
import com.defensemanagement.defense.defense.dto.DefenseStatus;
import com.defensemanagement.defense.defense.dto.GradeRequest;
import com.defensemanagement.defense.defense.dto.JuryAssignmentRequest;
import com.defensemanagement.defense.defense.dto.JuryDashboardStatus;
import com.defensemanagement.defense.defense.service.DefenseService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class DefenseController {
    private final DefenseService defenseService;
    private final AcademicClient academicClient;

    @GetMapping("/api/v1/defenses")
    @PreAuthorize("authenticated")
    public ResponseEntity<List<DefenseResponse>> getAll(
            @RequestParam(required = false) DefenseStatus status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(defenseService.getAll(status, date));
    }

    @GetMapping("/api/v1/defenses/{id}")
    @PreAuthorize("authenticated")
    public ResponseEntity<DefenseResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(defenseService.getById(id));
    }

    @PostMapping("/api/v1/defenses")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<DefenseResponse> create(HttpServletRequest servletRequest, @Valid @RequestBody DefenseRequest request) {
        return ResponseEntity.status(201).body(defenseService.create(request, buildRequestContext(servletRequest)));
    }

    @PutMapping("/api/v1/defenses/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<DefenseResponse> update(HttpServletRequest servletRequest,
                                                  @PathVariable Long id,
                                                  @Valid @RequestBody DefenseRequest request) {
        return ResponseEntity.ok(defenseService.update(id, request, buildRequestContext(servletRequest)));
    }

    @DeleteMapping("/api/v1/defenses/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        defenseService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/api/v1/defenses/check-conflicts")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<ConflictCheckResponse> checkConflicts(@Valid @RequestBody CheckConflictsRequest request) {
        return ResponseEntity.ok(defenseService.checkConflicts(request));
    }

    @GetMapping("/api/v1/defenses/{id}/jury")
    @PreAuthorize("authenticated")
    public ResponseEntity<JuryAssignmentRequest> getJury(@PathVariable Long id) {
        return ResponseEntity.ok(defenseService.getJury(id));
    }

    @PutMapping("/api/v1/defenses/{id}/jury")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<JuryAssignmentRequest> updateJury(HttpServletRequest servletRequest,
                                                            @PathVariable Long id,
                                                            @Valid @RequestBody JuryAssignmentRequest request) {
        return ResponseEntity.ok(defenseService.updateJury(id, request, buildRequestContext(servletRequest)));
    }

    @PutMapping("/api/v1/defenses/{id}/grades/president")
    @PreAuthorize("hasAuthority('ROLE_PROFESSOR')")
    public ResponseEntity<DefenseResponse> submitPresidentGrade(HttpServletRequest servletRequest,
                                                               @PathVariable Long id,
                                                               @Valid @RequestBody GradeRequest request) {
        ensureProfessorAssigned(servletRequest, id, "president");
        return ResponseEntity.ok(defenseService.submitPresidentGrade(id, request.getGrade()));
    }

    @PutMapping("/api/v1/defenses/{id}/grades/reviewer")
    @PreAuthorize("hasAuthority('ROLE_PROFESSOR')")
    public ResponseEntity<DefenseResponse> submitReviewerGrade(HttpServletRequest servletRequest,
                                                              @PathVariable Long id,
                                                              @Valid @RequestBody GradeRequest request) {
        ensureProfessorAssigned(servletRequest, id, "reviewer");
        return ResponseEntity.ok(defenseService.submitReviewerGrade(id, request.getGrade()));
    }

    @PutMapping("/api/v1/defenses/{id}/grades/examiner")
    @PreAuthorize("hasAuthority('ROLE_PROFESSOR')")
    public ResponseEntity<DefenseResponse> submitExaminerGrade(HttpServletRequest servletRequest,
                                                              @PathVariable Long id,
                                                              @Valid @RequestBody GradeRequest request) {
        ensureProfessorAssigned(servletRequest, id, "examiner");
        return ResponseEntity.ok(defenseService.submitExaminerGrade(id, request.getGrade()));
    }

    @PutMapping("/api/v1/defenses/{id}/grades/supervisor")
    @PreAuthorize("hasAuthority('ROLE_PROFESSOR')")
    public ResponseEntity<DefenseResponse> submitSupervisorGrade(HttpServletRequest servletRequest,
                                                                @PathVariable Long id,
                                                                @Valid @RequestBody GradeRequest request) {
        ensureProfessorAssigned(servletRequest, id, "supervisor");
        return ResponseEntity.ok(defenseService.submitSupervisorGrade(id, request.getGrade()));
    }

    @GetMapping("/api/v1/defenses/{id}/grades")
    @PreAuthorize("authenticated")
    public ResponseEntity<DefenseGradesResponse> getGrades(@PathVariable Long id) {
        return ResponseEntity.ok(defenseService.getGrades(id));
    }

    @PostMapping("/api/v1/defenses/{id}/publish")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<DefenseResponse> publish(@PathVariable Long id) {
        return ResponseEntity.ok(defenseService.publish(id));
    }

    @GetMapping("/api/v1/defenses/{id}/result")
    @PreAuthorize("authenticated")
    public ResponseEntity<DefenseResponse> getResult(@PathVariable Long id) {
        return ResponseEntity.ok(defenseService.getPublishedResult(id));
    }

    @GetMapping("/api/v1/students/{studentId}/result")
    @PreAuthorize("authenticated")
    public ResponseEntity<DefenseResponse> getByStudent(@PathVariable Long studentId) {
        return ResponseEntity.ok(defenseService.getByStudent(studentId));
    }

    @GetMapping("/api/v1/students/me/result")
    @PreAuthorize("hasAuthority('ROLE_STUDENT')")
    public ResponseEntity<DefenseResponse> getMyResult(HttpServletRequest request) {
        return ResponseEntity.ok(defenseService.getMyResult(buildRequestContext(request)));
    }

    @GetMapping("/api/v1/jury/defenses")
    @PreAuthorize("hasAuthority('ROLE_PROFESSOR')")
    public ResponseEntity<List<DefenseResponse>> getJuryDefenses(
            HttpServletRequest request,
            @RequestParam(required = false) JuryDashboardStatus status) {
        return ResponseEntity.ok(defenseService.getJuryDefenses(buildRequestContext(request), status));
    }

    @GetMapping("/api/v1/defenses/available-jury-members")
    @PreAuthorize("authenticated")
    public ResponseEntity<List<AvailableProfessorResponse>> getAvailableJuryMembers(
            HttpServletRequest request,
            @RequestParam String role,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime startTime,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime endTime,
            @RequestParam(required = false) Long excludeDefenseId) {
        return ResponseEntity.ok(defenseService.getAvailableJuryMembers(buildRequestContext(request), role, date, startTime, endTime, excludeDefenseId));
    }

    private AcademicClient.RequestContext buildRequestContext(HttpServletRequest request) {
        return AcademicClient.RequestContext.builder()
                .userId(request.getHeader("X-User-Id"))
                .username(request.getHeader("X-User-Username"))
                .roles(request.getHeader("X-User-Roles"))
                .requestId(request.getHeader("X-Request-Id"))
                .build();
    }

    private void ensureProfessorAssigned(HttpServletRequest request, Long defenseId, String role) {
        Long professorId = academicClient.getCurrentProfessor(buildRequestContext(request)).getId();
        DefenseResponse defense = defenseService.getById(defenseId);

        boolean authorized = switch (role) {
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
}
