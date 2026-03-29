package com.university.room_service.service;

import com.university.room_service.dto.RoomRequest;
import com.university.room_service.dto.RoomResponse;
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

    boolean isRoomAvailable(Long roomId, LocalDate date, LocalTime startTime, LocalTime endTime);
}
