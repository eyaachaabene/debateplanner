package com.defense.orchestrator.client;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class RoomClient {

    private final WebClient.Builder webClientBuilder;

    @Value("${room.service.url:http://localhost:8083}")
    private String roomServiceUrl;

    public Mono<Void> ensureRoomExists(Long roomId, HttpHeaders headers) {
    return webClientBuilder.build()
            .get()
            .uri(roomServiceUrl + "/api/v1/rooms/{id}", roomId)
            .headers(http -> copyHeaders(headers, http))
            .retrieve()
            .onStatus(HttpStatusCode::isError, response -> 
                response.bodyToMono(String.class)
                    .flatMap(errorBody -> Mono.error(new RuntimeException(errorBody)))
            )
            .bodyToMono(Void.class);
}

    private void copyHeaders(HttpHeaders source, HttpHeaders target) {
        source.forEach((name, values) -> values.forEach(value -> target.add(name, value)));
    }
}
