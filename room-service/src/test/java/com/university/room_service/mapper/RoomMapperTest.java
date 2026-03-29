package com.university.room_service.mapper;

import com.university.room_service.dto.RoomRequest;
import com.university.room_service.dto.RoomResponse;
import com.university.room_service.model.Room;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("RoomMapper unit tests")
class RoomMapperTest {

    private final RoomMapper roomMapper = new RoomMapper();

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

    private Room buildRoom(Long id, String name, int capacity) {
        return Room.builder()
                .id(id)
                .name(name)
                .capacity(capacity)
                .build();
    }

    @Nested
    @DisplayName("toResponse")
    class ToResponse {

        @Test
        @DisplayName("toResponse - should map id, name, and capacity correctly")
        void shouldMapAllFields() {
            // Arrange
            Room room = buildRoom(1L, "A101", 40);

            // Act
            RoomResponse actual = roomMapper.toResponse(room);

            // Assert
            assertThat(actual).isNotNull();
            assertThat(actual.getId()).isEqualTo(1L);
            assertThat(actual.getName()).isEqualTo("A101");
            assertThat(actual.getCapacity()).isEqualTo(40);
        }

        @Test
        @DisplayName("toResponse - should throw NullPointerException when input room is null")
        void shouldThrowWhenInputIsNull() {
            // Arrange
            Room room = null;

            // Act + Assert
            assertThatThrownBy(() -> roomMapper.toResponse(room))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("room must not be null");
        }
    }

    @Nested
    @DisplayName("toEntity")
    class ToEntity {

        @Test
        @DisplayName("toEntity - should map name and capacity and keep id null")
        void shouldMapRequestToEntity() {
            // Arrange
            RoomRequest request = buildRequest("B202", 60);

            // Act
            Room actual = roomMapper.toEntity(request);

            // Assert
            assertThat(actual).isNotNull();
            assertThat(actual.getId()).isNull();
            assertThat(actual.getName()).isEqualTo("B202");
            assertThat(actual.getCapacity()).isEqualTo(60);
        }
    }

    @Nested
    @DisplayName("toResponseList")
    class ToResponseList {

        @Test
        @DisplayName("toResponseList - should map a list of three rooms to three responses")
        void shouldMapThreeRooms() {
            // Arrange
            List<Room> rooms = List.of(
                    buildRoom(1L, "A101", 40),
                    buildRoom(2L, "B202", 50),
                    buildRoom(3L, "C303", 60)
            );

            // Act
            List<RoomResponse> responses = roomMapper.toResponseList(rooms);

            // Assert
            assertThat(responses).hasSize(3);
            assertThat(responses.get(0).getName()).isEqualTo("A101");
            assertThat(responses.get(1).getName()).isEqualTo("B202");
            assertThat(responses.get(2).getName()).isEqualTo("C303");
        }

        @Test
        @DisplayName("toResponseList - should return empty list when input list is empty")
        void shouldReturnEmptyList() {
            // Arrange
            List<Room> rooms = List.of();

            // Act
            List<RoomResponse> responses = roomMapper.toResponseList(rooms);

            // Assert
            assertThat(responses).isEmpty();
        }
    }
}
