package com.defense.orchestrator.service;

import com.defense.orchestrator.client.AcademicClient;
import com.defense.orchestrator.client.AuthClient;
import com.defense.orchestrator.dto.AcademicProfessorResponse;
import com.defense.orchestrator.dto.AcademicStudentResponse;
import com.defense.orchestrator.dto.ProfessorRequest;
import com.defense.orchestrator.dto.StudentRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class UserOrchestratorService {

    private static final String DEFAULT_PASSWORD = "ChangeMe123!";
    private final AuthClient authClient;
    private final AcademicClient academicClient;

    public Mono<AcademicStudentResponse> createStudent(StudentRequest request, HttpHeaders headers) {
        System.out.println("serviceCreating student with email: " + request.getEmail());
        Mono<Long> userIdMono = request.getUserId() != null
                ? Mono.just(request.getUserId())
                : authClient.registerUser(request.getEmail(), DEFAULT_PASSWORD, "STUDENT", headers);

        return userIdMono.flatMap(userId -> {
            request.setUserId(userId);
            return academicClient.createStudent(request, headers);
        });
    }

    public Mono<AcademicProfessorResponse> createProfessor(ProfessorRequest request, HttpHeaders headers) {
        System.out.println( "serviceCreating professor with email: " + request.getEmail());
        Mono<Long> userIdMono = request.getUserId() != null
                ? Mono.just(request.getUserId())
                : authClient.registerUser(request.getEmail(), DEFAULT_PASSWORD, "PROFESSOR", headers);

        return userIdMono.flatMap(userId -> {
            request.setUserId(userId);
            return academicClient.createProfessor(request, headers);
        });
    }
}
