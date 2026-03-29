package com.university.room_service.service.impl;

import com.university.room_service.dto.RoomRequest;
import com.university.room_service.dto.RoomResponse;
import com.university.room_service.exception.RoomAlreadyExistsException;
import com.university.room_service.exception.RoomNotFoundException;
import com.university.room_service.mapper.RoomMapper;
import com.university.room_service.model.Room;
import com.university.room_service.repository.RoomRepository;
import com.university.room_service.service.RoomService;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class RoomServiceImpl implements RoomService {

    private final RoomRepository roomRepository;
    private final RoomMapper roomMapper;

    @Override
    public RoomResponse createRoom(RoomRequest request) {
        if (roomRepository.existsByName(request.getName())) {
            throw new RoomAlreadyExistsException("Room with name '" + request.getName() + "' already exists");
        }

        Room room = roomMapper.toEntity(request);
        Room savedRoom = roomRepository.save(room);
        log.info("Created room with id={} and name={}", savedRoom.getId(), savedRoom.getName());
        return roomMapper.toResponse(savedRoom);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RoomResponse> getAllRooms() {
        return roomMapper.toResponseList(roomRepository.findAll());
    }

    @Override
    @Transactional(readOnly = true)
    public RoomResponse getRoomById(Long id) {
        Room room = roomRepository.findById(id)
                .orElseThrow(() -> new RoomNotFoundException("Room with id " + id + " not found"));
        return roomMapper.toResponse(room);
    }

    @Override
    public RoomResponse updateRoom(Long id, RoomRequest request) {
        Room existingRoom = roomRepository.findById(id)
                .orElseThrow(() -> new RoomNotFoundException("Room with id " + id + " not found"));

        if (roomRepository.existsByNameAndIdNot(request.getName(), id)) {
            throw new RoomAlreadyExistsException("Room with name '" + request.getName() + "' already exists");
        }

        existingRoom.setName(request.getName());
        existingRoom.setCapacity(request.getCapacity());

        Room updatedRoom = roomRepository.save(existingRoom);
        log.info("Updated room with id={}", id);
        return roomMapper.toResponse(updatedRoom);
    }

    @Override
    public void deleteRoom(Long id) {
        Room room = roomRepository.findById(id)
                .orElseThrow(() -> new RoomNotFoundException("Room with id " + id + " not found"));
        roomRepository.delete(room);
        log.info("Deleted room with id={} and name={}", room.getId(), room.getName());
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsById(Long id) {
        return roomRepository.existsById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isRoomAvailable(Long roomId, LocalDate date, LocalTime startTime, LocalTime endTime) {
        return true;
    }
}
