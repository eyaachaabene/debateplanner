package com.defense.orchestrator.client;

import com.defense.orchestrator.dto.DefenseRequest;
import com.defense.orchestrator.dto.DefenseResponse;
import com.defense.orchestrator.dto.JuryAssignmentRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
public class DefenseClient {
    private final WebClient.Builder webClientBuilder;

    @Value("${defense.service.url:http://localhost:8084}")
    private String defenseServiceUrl;

    public DefenseClient(WebClient.Builder webClientBuilder) {
        this.webClientBuilder = webClientBuilder;
    }

    public Mono<DefenseResponse> createDefense(DefenseRequest request, HttpHeaders headers) {
        return webClientBuilder.build()
                .post()
                .uri(defenseServiceUrl + "/api/v1/defenses")
                .headers(http -> copyHeaders(headers, http))
                .bodyValue(request)
                .retrieve()
                .onStatus(HttpStatusCode::isError, response -> 
                    response.bodyToMono(String.class)
                        .flatMap(errorBody -> Mono.error(new RuntimeException(errorBody)))
                )
                .bodyToMono(DefenseResponse.class);
    }

    public Mono<DefenseResponse> updateDefense(Long id, DefenseRequest request, HttpHeaders headers) {
        return webClientBuilder.build()
                .put()
                .uri(defenseServiceUrl + "/api/v1/defenses/{id}", id)
                .headers(http -> copyHeaders(headers, http))
                .bodyValue(request)
                .retrieve()
                .onStatus(HttpStatusCode::isError, response -> 
                    response.bodyToMono(String.class)
                        .flatMap(errorBody -> Mono.error(new RuntimeException(errorBody)))
                )
                .bodyToMono(DefenseResponse.class);
    }

    public Mono<JuryAssignmentRequest> updateJury(Long id, JuryAssignmentRequest request, HttpHeaders headers) {
        return webClientBuilder.build()
                .put()
                .uri(defenseServiceUrl + "/api/v1/defenses/{id}/jury", id)
                .headers(http -> copyHeaders(headers, http))
                .bodyValue(request)
                .retrieve()
                .onStatus(HttpStatusCode::isError, response -> 
                    response.bodyToMono(String.class)
                        .flatMap(errorBody -> Mono.error(new RuntimeException(errorBody)))
                )
                .bodyToMono(JuryAssignmentRequest.class);
    }

    public Mono<DefenseResponse> submitGrade(Long id, String role, Double grade, HttpHeaders headers) {
        return webClientBuilder.build()
                .put()
                .uri(defenseServiceUrl + "/api/v1/defenses/{id}/grades/{role}", id, role)
                .headers(http -> copyHeaders(headers, http))
                .bodyValue(java.util.Map.of("grade", grade))
                .retrieve()
                .onStatus(HttpStatusCode::isError, response -> 
                    response.bodyToMono(String.class)
                        .flatMap(errorBody -> Mono.error(new RuntimeException(errorBody)))
                )
                .bodyToMono(DefenseResponse.class);
    }

    public Mono<DefenseResponse> getByStudent(Long studentId, HttpHeaders headers) {
        return webClientBuilder.build()
                .get()
                .uri(defenseServiceUrl + "/api/v1/students/{studentId}/result", studentId)
                .headers(http -> copyHeaders(headers, http))
                .retrieve()
                .onStatus(HttpStatusCode::isError, response -> 
                    response.bodyToMono(String.class)
                        .flatMap(errorBody -> Mono.error(new RuntimeException(errorBody)))
                )
                .bodyToMono(DefenseResponse.class);
    }

    public Mono<DefenseResponse> getById(Long id, HttpHeaders headers) {
        return webClientBuilder.build()
                .get()
                .uri(defenseServiceUrl + "/api/v1/defenses/{id}", id)
                .headers(http -> copyHeaders(headers, http))
                .retrieve()
                .onStatus(HttpStatusCode::isError, response -> 
                    response.bodyToMono(String.class)
                        .flatMap(errorBody -> Mono.error(new RuntimeException(errorBody)))
                )
                .bodyToMono(DefenseResponse.class);
    }

    public Flux<DefenseResponse> getAllDefenses(HttpHeaders headers) {
        return webClientBuilder.build()
                .get()
                .uri(defenseServiceUrl + "/api/v1/defenses")
                .headers(http -> copyHeaders(headers, http))
                .retrieve()
                .onStatus(HttpStatusCode::isError, response -> 
                    response.bodyToMono(String.class)
                        .flatMap(errorBody -> Mono.error(new RuntimeException(errorBody)))
                )
                .bodyToFlux(DefenseResponse.class);
    }

    private void copyHeaders(HttpHeaders source, HttpHeaders target) {
        source.forEach((name, values) -> values.forEach(value -> target.add(name, value)));
    }
}