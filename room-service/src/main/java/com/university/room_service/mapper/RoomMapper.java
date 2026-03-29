package com.university.room_service.mapper;

import com.university.room_service.dto.RoomRequest;
import com.university.room_service.dto.RoomResponse;
import com.university.room_service.model.Room;
import java.util.List;
import java.util.Objects;
import org.springframework.stereotype.Component;

@Component
public class RoomMapper {

    public RoomResponse toResponse(Room room) {
        Objects.requireNonNull(room, "room must not be null");

        return RoomResponse.builder()
                .id(room.getId())
                .name(room.getName())
                .capacity(room.getCapacity())
                .build();
    }

    public Room toEntity(RoomRequest request) {
        if (request == null) {
            return null;
        }

        return Room.builder()
                .name(request.getName())
                .capacity(request.getCapacity())
                .build();
    }

    public List<RoomResponse> toResponseList(List<Room> rooms) {
        return rooms.stream().map(this::toResponse).toList();
    }
}
