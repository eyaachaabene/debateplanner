package com.university.room_service.service.impl;

import com.university.room_service.dto.RoomRequest;
import com.university.room_service.dto.RoomResponse;
import com.university.room_service.exception.RoomAlreadyExistsException;
import com.university.room_service.exception.RoomNotFoundException;
import com.university.room_service.mapper.RoomMapper;
import com.university.room_service.model.Room;
import com.university.room_service.model.RoomBooking;
import com.university.room_service.repository.RoomRepository;
import com.university.room_service.repository.RoomBookingRepository;
import com.university.room_service.service.RoomService;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;
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
    private final RoomBookingRepository roomBookingRepository;
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
        log.info("Fetching all rooms");
        List<RoomResponse> rooms = roomMapper.toResponseList(roomRepository.findAll());
        log.info("Found {} rooms", rooms.size());
        return rooms;
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
    public boolean isRoomAvailable(Long roomId, LocalDate date, LocalTime startTime, LocalTime endTime, Long excludeDefenseId) {
        List<RoomBooking> conflicts = roomBookingRepository.findConflictingBookings(
                roomId, date, startTime, endTime, excludeDefenseId != null ? excludeDefenseId : -1L);
        return conflicts.isEmpty();
    }

    @Override
    @Transactional(readOnly = true)
    public List<RoomResponse> getAvailableRooms(LocalDate date, LocalTime startTime,
                                                LocalTime endTime, Long excludeDefenseId) {
        log.info("Fetching available rooms for date={} from={} to={} excludingDefense={}",
                date, startTime, endTime, excludeDefenseId);

        return roomRepository.findAll().stream()
                .filter(room -> isRoomAvailable(room.getId(), date, startTime, endTime, excludeDefenseId))
                .map(roomMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public RoomBooking bookRoom(Long roomId, Long defenseId, LocalDate date, LocalTime startTime, LocalTime endTime) {
        RoomBooking booking = RoomBooking.builder()
                .roomId(roomId)
                .defenseId(defenseId)
                .defenseDate(date)
                .startTime(startTime)
                .endTime(endTime)
                .build();
        return roomBookingRepository.save(booking);
    }

    @Override
    public void cancelBooking(Long defenseId) {
        roomBookingRepository.deleteByDefenseId(defenseId);
    }
}
