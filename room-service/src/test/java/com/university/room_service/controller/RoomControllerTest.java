package com.university.room_service.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.university.room_service.dto.RoomRequest;
import com.university.room_service.dto.RoomResponse;
import com.university.room_service.exception.RoomAlreadyExistsException;
import com.university.room_service.exception.RoomNotFoundException;
import com.university.room_service.service.RoomService;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class RoomControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private RoomService roomService;

    private RoomRequest buildRequest(String name, int capacity) {
        return new RoomRequest(name, capacity);
    }

    private RoomResponse buildResponse(Long id, String name, int capacity) {
        return RoomResponse.builder()
                .id(id)
                .name(name)
                .capacity(capacity)
                .build();
    }

    @Nested
    @DisplayName("POST /api/v1/rooms")
    class CreateRoomEndpoint {

        @Test
        @DisplayName("POST /api/v1/rooms - should return 201 with room body for valid request")
        void shouldCreateRoom() throws Exception {
            // Arrange
            RoomRequest request = buildRequest("A101", 40);
            RoomResponse response = buildResponse(1L, "A101", 40);
            when(roomService.createRoom(any(RoomRequest.class))).thenReturn(response);

            // Act + Assert
            mockMvc.perform(post("/api/v1/rooms")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").value(1L))
                    .andExpect(jsonPath("$.name").value("A101"))
                    .andExpect(jsonPath("$.capacity").value(40));
        }

        @Test
        @DisplayName("POST /api/v1/rooms - should return 400 when name is blank")
        void shouldReturnBadRequestForBlankName() throws Exception {
            // Arrange
            RoomRequest request = buildRequest("   ", 40);

            // Act + Assert
            mockMvc.perform(post("/api/v1/rooms")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());

            verify(roomService, never()).createRoom(any(RoomRequest.class));
        }

        @Test
        @DisplayName("POST /api/v1/rooms - should return 400 when capacity is zero")
        void shouldReturnBadRequestForZeroCapacity() throws Exception {
            // Arrange
            RoomRequest request = buildRequest("A101", 0);

            // Act + Assert
            mockMvc.perform(post("/api/v1/rooms")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());

            verify(roomService, never()).createRoom(any(RoomRequest.class));
        }

        @Test
        @DisplayName("POST /api/v1/rooms - should return 400 when capacity is negative")
        void shouldReturnBadRequestForNegativeCapacity() throws Exception {
            // Arrange
            RoomRequest request = buildRequest("A101", -5);

            // Act + Assert
            mockMvc.perform(post("/api/v1/rooms")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());

            verify(roomService, never()).createRoom(any(RoomRequest.class));
        }

        @Test
        @DisplayName("POST /api/v1/rooms - should return 409 when duplicate room name is provided")
        void shouldReturnConflictForDuplicateName() throws Exception {
            // Arrange
            RoomRequest request = buildRequest("A101", 40);
            when(roomService.createRoom(any(RoomRequest.class)))
                    .thenThrow(new RoomAlreadyExistsException("Room with name 'A101' already exists"));

            // Act + Assert
            mockMvc.perform(post("/api/v1/rooms")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isConflict());
        }
    }

    @Nested
    @DisplayName("GET /api/v1/rooms")
    class GetAllRoomsEndpoint {

        @Test
        @DisplayName("GET /api/v1/rooms - should return 200 with list of rooms")
        void shouldReturnRoomList() throws Exception {
            // Arrange
            List<RoomResponse> responses = List.of(
                    buildResponse(1L, "A101", 40),
                    buildResponse(2L, "B202", 60)
            );
            when(roomService.getAllRooms()).thenReturn(responses);

            // Act + Assert
            mockMvc.perform(get("/api/v1/rooms"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].id").value(1L))
                    .andExpect(jsonPath("$[0].name").value("A101"))
                    .andExpect(jsonPath("$[1].id").value(2L))
                    .andExpect(jsonPath("$[1].name").value("B202"));
        }

        @Test
        @DisplayName("GET /api/v1/rooms - should return 200 with empty array when no rooms exist")
        void shouldReturnEmptyArray() throws Exception {
            // Arrange
            when(roomService.getAllRooms()).thenReturn(List.of());

            // Act + Assert
            mockMvc.perform(get("/api/v1/rooms"))
                    .andExpect(status().isOk())
                    .andExpect(content().json("[]"));
        }
    }

    @Nested
    @DisplayName("GET /api/v1/rooms/{id}")
    class GetRoomByIdEndpoint {

        @Test
        @DisplayName("GET /api/v1/rooms/{id} - should return 200 when room is found")
        void shouldReturnRoomById() throws Exception {
            // Arrange
            when(roomService.getRoomById(1L)).thenReturn(buildResponse(1L, "A101", 40));

            // Act + Assert
            mockMvc.perform(get("/api/v1/rooms/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1L))
                    .andExpect(jsonPath("$.name").value("A101"))
                    .andExpect(jsonPath("$.capacity").value(40));
        }

        @Test
        @DisplayName("GET /api/v1/rooms/{id} - should return 404 when room is not found")
        void shouldReturnNotFoundWhenRoomMissing() throws Exception {
            // Arrange
            when(roomService.getRoomById(99L)).thenThrow(new RoomNotFoundException("Room with id 99 not found"));

            // Act + Assert
            mockMvc.perform(get("/api/v1/rooms/99"))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("GET /api/v1/rooms/{id}/exists")
    class ExistsEndpoint {

        @Test
        @DisplayName("GET /api/v1/rooms/{id}/exists - should return true when room exists")
        void shouldReturnTrueForExistingRoom() throws Exception {
            // Arrange
            when(roomService.existsById(1L)).thenReturn(true);

            // Act + Assert
            mockMvc.perform(get("/api/v1/rooms/1/exists"))
                    .andExpect(status().isOk())
                    .andExpect(content().string("true"));
        }

        @Test
        @DisplayName("GET /api/v1/rooms/{id}/exists - should return false when room does not exist")
        void shouldReturnFalseForMissingRoom() throws Exception {
            // Arrange
            when(roomService.existsById(2L)).thenReturn(false);

            // Act + Assert
            mockMvc.perform(get("/api/v1/rooms/2/exists"))
                    .andExpect(status().isOk())
                    .andExpect(content().string("false"));
        }
    }

    @Nested
    @DisplayName("PUT /api/v1/rooms/{id}")
    class UpdateRoomEndpoint {

        @Test
        @DisplayName("PUT /api/v1/rooms/{id} - should return 200 with updated room for valid request")
        void shouldUpdateRoom() throws Exception {
            // Arrange
            RoomRequest request = buildRequest("A101-Updated", 55);
            RoomResponse response = buildResponse(1L, "A101-Updated", 55);
            when(roomService.updateRoom(eq(1L), any(RoomRequest.class))).thenReturn(response);

            // Act + Assert
            mockMvc.perform(put("/api/v1/rooms/1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1L))
                    .andExpect(jsonPath("$.name").value("A101-Updated"))
                    .andExpect(jsonPath("$.capacity").value(55));
        }

        @Test
        @DisplayName("PUT /api/v1/rooms/{id} - should return 404 when room id is not found")
        void shouldReturnNotFoundOnUpdateWhenMissing() throws Exception {
            // Arrange
            RoomRequest request = buildRequest("A101", 40);
            when(roomService.updateRoom(eq(100L), any(RoomRequest.class)))
                    .thenThrow(new RoomNotFoundException("Room with id 100 not found"));

            // Act + Assert
            mockMvc.perform(put("/api/v1/rooms/100")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("PUT /api/v1/rooms/{id} - should return 409 when updated name already exists")
        void shouldReturnConflictOnUpdateWhenDuplicateName() throws Exception {
            // Arrange
            RoomRequest request = buildRequest("A101", 40);
            when(roomService.updateRoom(eq(1L), any(RoomRequest.class)))
                    .thenThrow(new RoomAlreadyExistsException("Room with name 'A101' already exists"));

            // Act + Assert
            mockMvc.perform(put("/api/v1/rooms/1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isConflict());
        }

        @Test
        @DisplayName("PUT /api/v1/rooms/{id} - should return 400 for invalid request with blank name")
        void shouldReturnBadRequestForInvalidUpdatePayload() throws Exception {
            // Arrange
            RoomRequest request = buildRequest("", 40);

            // Act + Assert
            mockMvc.perform(put("/api/v1/rooms/1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());

            verify(roomService, never()).updateRoom(eq(1L), any(RoomRequest.class));
        }
    }

    @Nested
    @DisplayName("DELETE /api/v1/rooms/{id}")
    class DeleteRoomEndpoint {

        @Test
        @DisplayName("DELETE /api/v1/rooms/{id} - should return 204 when room exists")
        void shouldDeleteRoom() throws Exception {
            // Arrange
            doNothing().when(roomService).deleteRoom(1L);

            // Act + Assert
            mockMvc.perform(delete("/api/v1/rooms/1"))
                    .andExpect(status().isNoContent())
                    .andExpect(content().string(""));

            verify(roomService).deleteRoom(1L);
        }

        @Test
        @DisplayName("DELETE /api/v1/rooms/{id} - should return 404 when room does not exist")
        void shouldReturnNotFoundWhenDeletingMissingRoom() throws Exception {
            // Arrange
            doThrow(new RoomNotFoundException("Room with id 404 not found"))
                    .when(roomService)
                    .deleteRoom(404L);

            // Act + Assert
            mockMvc.perform(delete("/api/v1/rooms/404"))
                    .andExpect(status().isNotFound());
        }
    }
}