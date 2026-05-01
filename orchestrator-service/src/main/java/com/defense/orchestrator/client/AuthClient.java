package com.defense.orchestrator.client;

import com.defense.orchestrator.dto.RegisterInternalRequest;
import com.defense.orchestrator.dto.RegisterInternalResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class AuthClient {

    private final WebClient.Builder webClientBuilder;

    @Value("${auth.service.url:http://localhost:8085}")
    private String authServiceUrl;

    @Value("${internal.secret:MY_SUPER_SECRET_123}")
    private String internalSecret;

    public Mono<Long> registerUser(String username, String password, String role, HttpHeaders headers) {
    RegisterInternalRequest request = new RegisterInternalRequest();
    request.setUsername(username);
    request.setPassword(password);
    request.setRole(role);
    System.out.println("Registering user with role: " + role);

    return webClientBuilder.build()
            .post()
            .uri(authServiceUrl + "/api/v1/auth/register-internal")
            .header("X-Internal-Secret", internalSecret)
            .headers(http -> copyHeaders(headers, http))
            .bodyValue(request)
            .retrieve()
            .onStatus(HttpStatusCode::isError, response -> 
                response.bodyToMono(String.class)
                    .flatMap(errorBody -> Mono.error(new RuntimeException(errorBody)))
            )
            .bodyToMono(RegisterInternalResponse.class)
            .map(RegisterInternalResponse::getUserId);
}

    private void copyHeaders(HttpHeaders source, HttpHeaders target) {
        source.forEach((name, values) -> values.forEach(value -> target.add(name, value)));
    }
}
