package com.university.room_service.service;

import com.university.room_service.dto.RoomRequest;
import com.university.room_service.dto.RoomResponse;
import com.university.room_service.model.RoomBooking;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public interface RoomService {

    RoomResponse createRoom(RoomRequest request);

    List<RoomResponse> getAllRooms();

    RoomResponse getRoomById(Long id);

    RoomResponse updateRoom(Long id, RoomRequest request);

    void deleteRoom(Long id);

    boolean existsById(Long id);

    boolean isRoomAvailable(Long roomId, LocalDate date, LocalTime startTime, LocalTime endTime, Long excludeDefenseId);

    List<RoomResponse> getAvailableRooms(LocalDate date, LocalTime startTime, LocalTime endTime, Long excludeDefenseId);

    RoomBooking bookRoom(Long roomId, Long defenseId, LocalDate date, LocalTime startTime, LocalTime endTime);

    void cancelBooking(Long defenseId);
}
