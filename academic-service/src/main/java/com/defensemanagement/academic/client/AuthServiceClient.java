package com.defensemanagement.academic.client;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class AuthServiceClient {

    private final WebClient.Builder webClientBuilder;

    @Value("${services.auth-url}")
    private String authServiceUrl;

    @Value("${internal.secret}")
    private String internalSecret;

    public Long registerUser(String username, String password, String role) {
        try {
            Map<String, String> body = Map.of(
                    "username", username,
                    "password", password,
                    "role", role
            );

            RegisterInternalResponse response = webClientBuilder.build()
                    .post()
                    .uri(authServiceUrl + "/api/v1/auth/register-internal")
                    .header("X-Internal-Secret", internalSecret)
                    .bodyValue(body)
                    .retrieve()
                    .bodyToMono(RegisterInternalResponse.class)
                    .block();

            if (response == null) {
                throw new RuntimeException("No response from auth-service");
            }

            return response.getUserId();
        } catch (WebClientResponseException e) {
            throw new RuntimeException("Auth service error: " + e.getResponseBodyAsString(), e);
        }
    }

    @lombok.Data
    static class RegisterInternalResponse {
        private Long userId;
    }
}