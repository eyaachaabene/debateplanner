package com.university.room_service.repository;

import com.university.room_service.model.RoomBooking;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface RoomBookingRepository extends JpaRepository<RoomBooking, Long> {

    List<RoomBooking> findByRoomIdAndDefenseDate(Long roomId, LocalDate date);

    boolean existsByDefenseId(Long defenseId);

    Optional<RoomBooking> findByDefenseId(Long defenseId);

    void deleteByDefenseId(Long defenseId);

    @Query("SELECT rb FROM RoomBooking rb WHERE rb.roomId = :roomId AND rb.defenseDate = :date AND rb.id != :excludeId AND NOT (rb.endTime <= :startTime OR rb.startTime >= :endTime)")
    List<RoomBooking> findConflictingBookings(
            @Param("roomId") Long roomId,
            @Param("date") LocalDate date,
            @Param("startTime") LocalTime startTime,
            @Param("endTime") LocalTime endTime,
            @Param("excludeId") Long excludeId
    );
}
