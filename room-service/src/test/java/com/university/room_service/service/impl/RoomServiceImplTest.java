package com.university.room_service.service.impl;

import com.university.room_service.dto.RoomRequest;
import com.university.room_service.dto.RoomResponse;
import com.university.room_service.exception.RoomAlreadyExistsException;
import com.university.room_service.exception.RoomNotFoundException;
import com.university.room_service.mapper.RoomMapper;
import com.university.room_service.model.Room;
import com.university.room_service.repository.RoomRepository;
import com.university.room_service.repository.RoomBookingRepository;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("RoomServiceImpl unit tests")
class RoomServiceImplTest {

    @Mock
    private RoomRepository roomRepository;

    @Mock
    private RoomBookingRepository roomBookingRepository;

    @Mock
    private RoomMapper roomMapper;

    @InjectMocks
    private RoomServiceImpl roomService;

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
    @DisplayName("createRoom")
    class CreateRoom {

        @Test
        @DisplayName("createRoom - should save and return mapped response when room name is unique")
        void shouldCreateRoomSuccessfully() {
            // Arrange
            RoomRequest request = buildRequest("A101", 40);
            Room roomToSave = buildRoom(null, "A101", 40);
            Room savedRoom = buildRoom(1L, "A101", 40);
            RoomResponse expectedResponse = buildResponse(1L, "A101", 40);

            when(roomRepository.existsByName("A101")).thenReturn(false);
            when(roomMapper.toEntity(request)).thenReturn(roomToSave);
            when(roomRepository.save(roomToSave)).thenReturn(savedRoom);
            when(roomMapper.toResponse(savedRoom)).thenReturn(expectedResponse);

            // Act
            RoomResponse actualResponse = roomService.createRoom(request);

            // Assert
            assertThat(actualResponse).isNotNull();
            assertThat(actualResponse.getId()).isEqualTo(1L);
            assertThat(actualResponse.getName()).isEqualTo("A101");
            assertThat(actualResponse.getCapacity()).isEqualTo(40);
            verify(roomRepository).save(roomToSave);
        }

        @Test
        @DisplayName("createRoom - should throw RoomAlreadyExistsException when name already exists")
        void shouldThrowWhenRoomNameAlreadyExists() {
            // Arrange
            RoomRequest request = buildRequest("A101", 40);
            when(roomRepository.existsByName("A101")).thenReturn(true);

            // Act + Assert
            assertThatThrownBy(() -> roomService.createRoom(request))
                    .isInstanceOf(RoomAlreadyExistsException.class)
                    .hasMessageContaining("A101");

            verify(roomRepository, never()).save(org.mockito.ArgumentMatchers.any(Room.class));
        }
    }

    @Nested
    @DisplayName("getAllRooms")
    class GetAllRooms {

        @Test
        @DisplayName("getAllRooms - should return mapped room responses")
        void shouldReturnMappedResponses() {
            // Arrange
            Room room1 = buildRoom(1L, "A101", 40);
            Room room2 = buildRoom(2L, "B202", 60);
            List<Room> rooms = List.of(room1, room2);
            List<RoomResponse> expectedResponses = List.of(
                    buildResponse(1L, "A101", 40),
                    buildResponse(2L, "B202", 60)
            );

            when(roomRepository.findAll()).thenReturn(rooms);
            when(roomMapper.toResponseList(rooms)).thenReturn(expectedResponses);

            // Act
            List<RoomResponse> actualResponses = roomService.getAllRooms();

            // Assert
            assertThat(actualResponses).hasSize(2);
            assertThat(actualResponses).isEqualTo(expectedResponses);
        }

        @Test
        @DisplayName("getAllRooms - should return empty list when repository has no rooms")
        void shouldReturnEmptyList() {
            // Arrange
            List<Room> emptyRooms = List.of();
            List<RoomResponse> emptyResponses = List.of();
            when(roomRepository.findAll()).thenReturn(emptyRooms);
            when(roomMapper.toResponseList(emptyRooms)).thenReturn(emptyResponses);

            // Act
            List<RoomResponse> actualResponses = roomService.getAllRooms();

            // Assert
            assertThat(actualResponses).isEmpty();
        }
    }

    @Nested
    @DisplayName("getRoomById")
    class GetRoomById {

        @Test
        @DisplayName("getRoomById - should return mapped response when room exists")
        void shouldReturnRoomWhenFound() {
            // Arrange
            Long roomId = 10L;
            Room existingRoom = buildRoom(roomId, "C303", 35);
            RoomResponse expectedResponse = buildResponse(roomId, "C303", 35);

            when(roomRepository.findById(roomId)).thenReturn(Optional.of(existingRoom));
            when(roomMapper.toResponse(existingRoom)).thenReturn(expectedResponse);

            // Act
            RoomResponse actualResponse = roomService.getRoomById(roomId);

            // Assert
            assertThat(actualResponse).isEqualTo(expectedResponse);
        }

        @Test
        @DisplayName("getRoomById - should throw RoomNotFoundException when room does not exist")
        void shouldThrowWhenRoomNotFound() {
            // Arrange
            Long roomId = 99L;
            when(roomRepository.findById(roomId)).thenReturn(Optional.empty());

            // Act + Assert
            assertThatThrownBy(() -> roomService.getRoomById(roomId))
                    .isInstanceOf(RoomNotFoundException.class)
                    .hasMessageContaining("99");
        }
    }

    @Nested
    @DisplayName("updateRoom")
    class UpdateRoom {

        @Test
        @DisplayName("updateRoom - should update and return mapped response when room exists and name is unique")
        void shouldUpdateRoomSuccessfully() {
            // Arrange
            Long roomId = 3L;
            RoomRequest request = buildRequest("D404", 80);
            Room existingRoom = buildRoom(roomId, "OldName", 20);
            Room updatedRoom = buildRoom(roomId, "D404", 80);
            RoomResponse expectedResponse = buildResponse(roomId, "D404", 80);

            when(roomRepository.findById(roomId)).thenReturn(Optional.of(existingRoom));
            when(roomRepository.existsByNameAndIdNot("D404", roomId)).thenReturn(false);
            when(roomRepository.save(existingRoom)).thenReturn(updatedRoom);
            when(roomMapper.toResponse(updatedRoom)).thenReturn(expectedResponse);

            // Act
            RoomResponse actualResponse = roomService.updateRoom(roomId, request);

            // Assert
            assertThat(existingRoom.getName()).isEqualTo("D404");
            assertThat(existingRoom.getCapacity()).isEqualTo(80);
            assertThat(actualResponse).isEqualTo(expectedResponse);
            verify(roomRepository).save(existingRoom);
        }

        @Test
        @DisplayName("updateRoom - should throw RoomNotFoundException when room id does not exist")
        void shouldThrowWhenUpdatingMissingRoom() {
            // Arrange
            Long roomId = 77L;
            RoomRequest request = buildRequest("E505", 30);
            when(roomRepository.findById(roomId)).thenReturn(Optional.empty());

            // Act + Assert
            assertThatThrownBy(() -> roomService.updateRoom(roomId, request))
                    .isInstanceOf(RoomNotFoundException.class)
                    .hasMessageContaining("77");

            verify(roomRepository, never()).save(org.mockito.ArgumentMatchers.any(Room.class));
        }

        @Test
        @DisplayName("updateRoom - should throw RoomAlreadyExistsException when another room has same name")
        void shouldThrowWhenNameTakenByAnotherRoom() {
            // Arrange
            Long roomId = 5L;
            RoomRequest request = buildRequest("F606", 45);
            Room existingRoom = buildRoom(roomId, "Original", 25);

            when(roomRepository.findById(roomId)).thenReturn(Optional.of(existingRoom));
            when(roomRepository.existsByNameAndIdNot("F606", roomId)).thenReturn(true);

            // Act + Assert
            assertThatThrownBy(() -> roomService.updateRoom(roomId, request))
                    .isInstanceOf(RoomAlreadyExistsException.class)
                    .hasMessageContaining("F606");

            verify(roomRepository, never()).save(org.mockito.ArgumentMatchers.any(Room.class));
        }
    }

    @Nested
    @DisplayName("deleteRoom")
    class DeleteRoom {

        @Test
        @DisplayName("deleteRoom - should delete room when it exists")
        void shouldDeleteRoomSuccessfully() {
            // Arrange
            Long roomId = 8L;
            Room existingRoom = buildRoom(roomId, "G707", 50);
            when(roomRepository.findById(roomId)).thenReturn(Optional.of(existingRoom));

            // Act
            roomService.deleteRoom(roomId);

            // Assert
            verify(roomRepository).delete(existingRoom);
        }

        @Test
        @DisplayName("deleteRoom - should throw RoomNotFoundException when room does not exist")
        void shouldThrowWhenDeletingMissingRoom() {
            // Arrange
            Long roomId = 404L;
            when(roomRepository.findById(roomId)).thenReturn(Optional.empty());

            // Act + Assert
            assertThatThrownBy(() -> roomService.deleteRoom(roomId))
                    .isInstanceOf(RoomNotFoundException.class)
                    .hasMessageContaining("404");

            verify(roomRepository, never()).delete(org.mockito.ArgumentMatchers.any(Room.class));
        }
    }

    @Nested
    @DisplayName("existsById")
    class ExistsById {

        @Test
        @DisplayName("existsById - should return true when room exists")
        void shouldReturnTrueWhenExists() {
            // Arrange
            Long roomId = 1L;
            when(roomRepository.existsById(roomId)).thenReturn(true);

            // Act
            boolean exists = roomService.existsById(roomId);

            // Assert
            assertThat(exists).isTrue();
        }

        @Test
        @DisplayName("existsById - should return false when room does not exist")
        void shouldReturnFalseWhenMissing() {
            // Arrange
            Long roomId = 2L;
            when(roomRepository.existsById(roomId)).thenReturn(false);

            // Act
            boolean exists = roomService.existsById(roomId);

            // Assert
            assertThat(exists).isFalse();
        }
    }

    @Nested
    @DisplayName("isRoomAvailable")
    class IsRoomAvailable {

        @Test
        @DisplayName("isRoomAvailable - should return true when no conflicting bookings exist")
        void shouldReturnTrueWhenNoConflicts() {
            // Arrange
            Long roomId = 1L;
            LocalDate date = LocalDate.of(2026, 3, 28);
            LocalTime startTime = LocalTime.of(10, 0);
            LocalTime endTime = LocalTime.of(11, 0);

            when(roomBookingRepository.findConflictingBookings(roomId, date, startTime, endTime, -1L))
                    .thenReturn(List.of());

            // Act
            boolean available = roomService.isRoomAvailable(roomId, date, startTime, endTime, null);

            // Assert
            assertThat(available).isTrue();
        }
    }
}
