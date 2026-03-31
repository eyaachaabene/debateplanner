package com.defensemanagement.defense.client;

import com.defensemanagement.defense.exception.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class RoomClient {
    private final RestClient restClient;

    public RoomClient(RestClient.Builder builder,
                      @Value("${services.room.base-url}") String roomBaseUrl) {
        this.restClient = builder.baseUrl(roomBaseUrl).build();
    }

    public void ensureRoomExists(Long roomId, AcademicClient.RequestContext requestContext) {
        restClient.get()
                .uri("/api/v1/rooms/{id}", roomId)
                .headers(requestContext::apply)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, (request, response) -> {
                    throw new ResourceNotFoundException("Room not found with id: " + roomId);
                })
                .toBodilessEntity();
    }
}
