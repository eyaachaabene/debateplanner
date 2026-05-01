package com.defense.orchestrator.client;

import com.defense.orchestrator.dto.AcademicProfessorRequest;
import com.defense.orchestrator.dto.AcademicProfessorResponse;
import com.defense.orchestrator.dto.AcademicStudentRequest;
import com.defense.orchestrator.dto.AcademicStudentResponse;
import com.defense.orchestrator.dto.ProfessorRequest;
import com.defense.orchestrator.dto.StudentRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class AcademicClient {

    private final WebClient.Builder webClientBuilder;

    @Value("${academic.service.url:http://localhost:8082}")
    private String academicServiceUrl;

    public Mono<AcademicStudentResponse> getStudentById(Long studentId, HttpHeaders headers) {
        return webClientBuilder.build()
                .get()
                .uri(academicServiceUrl + "/api/v1/students/{id}", studentId)
                .headers(http -> copyHeaders(headers, http))
                .retrieve()
                .onStatus(HttpStatusCode::isError, response -> 
                    response.bodyToMono(String.class)
                        .flatMap(errorBody -> Mono.error(new RuntimeException(errorBody)))
                )
                .bodyToMono(AcademicStudentResponse.class);
    }

    public Mono<AcademicProfessorResponse> getProfessorById(Long professorId, HttpHeaders headers) {
        return webClientBuilder.build()
                .get()
                .uri(academicServiceUrl + "/api/v1/professors/{id}", professorId)
                .headers(http -> copyHeaders(headers, http))
                .retrieve()
                .onStatus(HttpStatusCode::isError, response -> 
                    response.bodyToMono(String.class)
                        .flatMap(errorBody -> Mono.error(new RuntimeException(errorBody)))
                )
                .bodyToMono(AcademicProfessorResponse.class);
    }

    public Mono<AcademicStudentResponse> getCurrentStudent(HttpHeaders headers) {
        return webClientBuilder.build()
                .get()
                .uri(academicServiceUrl + "/api/v1/students/me")
                .headers(http -> copyHeaders(headers, http))
                .retrieve()
                .onStatus(HttpStatusCode::isError, response -> 
                    response.bodyToMono(String.class)
                        .flatMap(errorBody -> Mono.error(new RuntimeException(errorBody)))
                )
                .bodyToMono(AcademicStudentResponse.class);
    }

    public Mono<AcademicProfessorResponse> getCurrentProfessor(HttpHeaders headers) {
        String url = (academicServiceUrl + "/api/v1/professors/me").trim();

        System.out.println(">>> academicServiceUrl = [" + academicServiceUrl + "]");
        System.out.println(">>> FINAL URL = [" + url + "]");
        System.out.println(">>> LENGTH = " + url.length());

        return webClientBuilder.build()
                .get()
                .uri(url)
                .headers(http -> copyHeaders(headers, http))
                .retrieve()
                .onStatus(HttpStatusCode::isError, response -> 
                    response.bodyToMono(String.class)
                            .flatMap(errorBody -> Mono.error(new RuntimeException(errorBody)))
                )
                .bodyToMono(AcademicProfessorResponse.class);
    }

    public Mono<AcademicStudentResponse> createStudent(StudentRequest request, HttpHeaders headers) {
        AcademicStudentRequest academicRequest = toAcademicStudentRequest(request);

        return webClientBuilder.build()
                .post()
                .uri(academicServiceUrl + "/api/v1/students")
                .headers(http -> copyHeaders(headers, http))
                .bodyValue(academicRequest)
                .retrieve()
                .onStatus(HttpStatusCode::isError, response -> 
                    response.bodyToMono(String.class)
                        .flatMap(errorBody -> Mono.error(new RuntimeException(errorBody)))
                )
                .bodyToMono(AcademicStudentResponse.class);
    }

    public Mono<AcademicProfessorResponse> createProfessor(ProfessorRequest request, HttpHeaders headers) {
        AcademicProfessorRequest academicRequest = toAcademicProfessorRequest(request);

        return webClientBuilder.build()
                .post()
                .uri(academicServiceUrl + "/api/v1/professors")
                .headers(http -> copyHeaders(headers, http))
                .bodyValue(academicRequest)
                .retrieve()
                .onStatus(HttpStatusCode::isError, response -> 
                    response.bodyToMono(String.class)
                        .flatMap(errorBody -> Mono.error(new RuntimeException(errorBody)))
                )
                .bodyToMono(AcademicProfessorResponse.class);
    }

    private AcademicStudentRequest toAcademicStudentRequest(StudentRequest request) {
        AcademicStudentRequest academicRequest = new AcademicStudentRequest();

        academicRequest.setUserId(request.getUserId());
        academicRequest.setFirstName(request.getFirstName());
        academicRequest.setLastName(request.getLastName());
        academicRequest.setEmail(request.getEmail());
        academicRequest.setMajor(request.getMajor()); // 🔥 KEY FIX
        academicRequest.setLevel(request.getLevel());

        return academicRequest;
    }

    private AcademicProfessorRequest toAcademicProfessorRequest(ProfessorRequest request) {
        AcademicProfessorRequest academicRequest = new AcademicProfessorRequest();
        academicRequest.setUserId(request.getUserId());
        academicRequest.setFirstName(request.getFirstName());
        academicRequest.setLastName(request.getLastName());
        academicRequest.setEmail(request.getEmail());
        return academicRequest;
    }

    public Flux<AcademicProfessorResponse> getAllProfessors(HttpHeaders headers) {
        return webClientBuilder.build()
                .get()
                .uri(academicServiceUrl + "/api/v1/professors")
                .headers(http -> copyHeaders(headers, http))
                .retrieve()
                .onStatus(HttpStatusCode::isError, response -> 
                    response.bodyToMono(String.class)
                        .flatMap(errorBody -> Mono.error(new RuntimeException(errorBody)))
                )
                .bodyToFlux(AcademicProfessorResponse.class);
    }

    private void copyHeaders(HttpHeaders source, HttpHeaders target) {
        source.forEach((name, values) -> values.forEach(value -> target.add(name, value)));
    }
}