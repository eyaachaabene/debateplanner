package com.defense.orchestrator.controller;

import com.defense.orchestrator.dto.AvailableProfessorResponse;
import com.defense.orchestrator.dto.DefenseRequest;
import com.defense.orchestrator.dto.DefenseResponse;
import com.defense.orchestrator.dto.GradeRequest;
import com.defense.orchestrator.dto.JuryAssignmentRequest;
import com.defense.orchestrator.dto.ProfessorRequest;
import com.defense.orchestrator.dto.StudentRequest;
import com.defense.orchestrator.service.DefenseOrchestratorService;
import com.defense.orchestrator.service.UserOrchestratorService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@RestController
@RequestMapping("/internal/orchestrations")
@RequiredArgsConstructor
public class OrchestrationController {

    private final UserOrchestratorService userOrchestratorService;
    private final DefenseOrchestratorService defenseOrchestratorService;

    @PostMapping("/users/students")
    public Mono<ResponseEntity<?>> createStudent(ServerHttpRequest request,@RequestBody StudentRequest body) {
        System.out.println("Received request to create student: " + body);
        return userOrchestratorService.createStudent(body, request.getHeaders())
                .map(response -> ResponseEntity.status(201).body(response));
    }

    @PostMapping("/users/professors")
    public Mono<ResponseEntity<?>> createProfessor(ServerHttpRequest request, @RequestBody ProfessorRequest body) {
        System.out.println("Received request to create professor: " + body);
        return userOrchestratorService.createProfessor(body, request.getHeaders())
                .map(response -> ResponseEntity.status(201).body(response));
    }

    @PostMapping("/defenses")
    public Mono<ResponseEntity<DefenseResponse>> createDefense(ServerHttpRequest request, @RequestBody DefenseRequest body) {
        return defenseOrchestratorService.createDefense(body, request.getHeaders())
                .map(response -> ResponseEntity.status(201).body(response));
    }

    @PutMapping("/defenses/{id}")
    public Mono<ResponseEntity<DefenseResponse>> updateDefense(ServerHttpRequest request,
                                                               @PathVariable Long id,
                                                               @RequestBody DefenseRequest body) {
        return defenseOrchestratorService.updateDefense(id, body, request.getHeaders())
                .map(ResponseEntity::ok);
    }

    @PutMapping("/defenses/{id}/jury")
    public Mono<ResponseEntity<JuryAssignmentRequest>> updateJury(ServerHttpRequest request,
                                                                  @PathVariable Long id,
                                                                  @RequestBody JuryAssignmentRequest body) {
        return defenseOrchestratorService.updateJury(id, body, request.getHeaders())
                .map(ResponseEntity::ok);
    }

    @PutMapping("/defenses/{id}/grades/{role}")
    public Mono<ResponseEntity<DefenseResponse>> submitGrade(ServerHttpRequest request,
                                                             @PathVariable Long id,
                                                             @PathVariable String role,
                                                             @RequestBody GradeRequest body) {
        
        System.out.println("=== CONTROLLER RECEIVED GRADE SUBMISSION ===");
        System.out.println("Defense ID: " + id);
        System.out.println("Role: " + role);
        System.out.println("Grade value: " + (body != null ? body.getGrade() : "null"));
        System.out.println("Auth headers present: " + request.getHeaders().containsKey(HttpHeaders.AUTHORIZATION));
        return defenseOrchestratorService.submitGrade(id, role, body, request.getHeaders())
                .map(ResponseEntity::ok);
    }

    @GetMapping("/students/me/result")
    public Mono<ResponseEntity<DefenseResponse>> getCurrentStudentResult(ServerHttpRequest request) {
        return defenseOrchestratorService.getCurrentStudentResult(request.getHeaders())
                .map(ResponseEntity::ok);
    }

    @GetMapping("/defenses")
    public Mono<ResponseEntity<List<DefenseResponse>>> getAllDefenses(ServerHttpRequest request) {
        return defenseOrchestratorService.getAllDefenses(request.getHeaders())
                .map(ResponseEntity::ok);
    }

    @GetMapping("/defenses/{id}")
    public Mono<ResponseEntity<DefenseResponse>> getDefenseById(ServerHttpRequest request,
                                                                 @PathVariable Long id) {
        System.out.println("Received request to get defense by id: " + id);
        return defenseOrchestratorService.getDefenseById(id, request.getHeaders())
                .map(ResponseEntity::ok);
    }

    @GetMapping("/jury/defenses")
    public Mono<ResponseEntity<List<DefenseResponse>>> getJuryDefenses(ServerHttpRequest request,
                                                                       @RequestParam(required = false) String status) {
        return defenseOrchestratorService.getJuryDefenses(request.getHeaders(), status)
                .map(ResponseEntity::ok);
    }

    @GetMapping("/defenses/available-jury-members")
    public Mono<ResponseEntity<List<AvailableProfessorResponse>>> getAvailableJuryMembers(ServerHttpRequest request,
                                                                                          @RequestParam String role,
                                                                                          @RequestParam LocalDate date,
                                                                                          @RequestParam LocalTime startTime,
                                                                                          @RequestParam LocalTime endTime,
                                                                                          @RequestParam(required = false) Long excludeDefenseId) {
        return defenseOrchestratorService.getAvailableJuryMembers(request.getHeaders(), role, date, startTime, endTime, excludeDefenseId)
                .map(ResponseEntity::ok);
    }
}
