package com.defensemanagement.defense.client;

import com.defensemanagement.defense.defense.dto.AvailableProfessorResponse;
import com.defensemanagement.defense.exception.ResourceNotFoundException;
import lombok.Builder;
import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Arrays;
import java.util.List;

@Component
public class AcademicClient {
    private final RestClient restClient;

    public AcademicClient(RestClient.Builder builder,
                          @Value("${services.academic.base-url}") String academicBaseUrl) {
        this.restClient = builder.baseUrl(academicBaseUrl).build();
    }

    public void ensureStudentExists(Long studentId, RequestContext requestContext) {
        restClient.get()
                .uri("/api/v1/students/{id}", studentId)
                .headers(requestContext::apply)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, (request, response) -> {
                    throw new ResourceNotFoundException("Student not found with id: " + studentId);
                })
                .toBodilessEntity();
    }

    public void ensureProfessorExists(Long professorId, RequestContext requestContext) {
        restClient.get()
                .uri("/api/v1/professors/{id}", professorId)
                .headers(requestContext::apply)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, (request, response) -> {
                    throw new ResourceNotFoundException("Professor not found with id: " + professorId);
                })
                .toBodilessEntity();
    }

    public CurrentStudentResponse getCurrentStudent(RequestContext requestContext) {
        return restClient.get()
                .uri("/api/v1/students/me")
                .headers(requestContext::apply)
                .retrieve()
                .body(CurrentStudentResponse.class);
    }

    public CurrentProfessorResponse getCurrentProfessor(RequestContext requestContext) {
        return restClient.get()
                .uri("/api/v1/professors/me")
                .headers(requestContext::apply)
                .retrieve()
                .body(CurrentProfessorResponse.class);
    }

    public List<AvailableProfessorResponse> getAllProfessors(RequestContext requestContext) {
        AvailableProfessorResponse[] response = restClient.get()
                .uri("/api/v1/professors")
                .headers(requestContext::apply)
                .retrieve()
                .body(AvailableProfessorResponse[].class);
        return response == null ? List.of() : Arrays.asList(response);
    }

    @Data
    public static class CurrentStudentResponse {
        private Long id;
        private Long userId;
        private String firstName;
        private String lastName;
        private String email;
    }

    @Data
    public static class CurrentProfessorResponse {
        private Long id;
        private Long userId;
        private String firstName;
        private String lastName;
        private String email;
    }

    @Data
    @Builder
    public static class RequestContext {
        private String userId;
        private String username;
        private String roles;
        private String requestId;

        public void apply(HttpHeaders headers) {
            if (userId != null && !userId.isBlank()) {
                headers.set("X-User-Id", userId);
            }
            if (username != null && !username.isBlank()) {
                headers.set("X-User-Username", username);
            }
            if (roles != null && !roles.isBlank()) {
                headers.set("X-User-Roles", roles);
            }
            if (requestId != null && !requestId.isBlank()) {
                headers.set("X-Request-Id", requestId);
            }
        }
    }
}
