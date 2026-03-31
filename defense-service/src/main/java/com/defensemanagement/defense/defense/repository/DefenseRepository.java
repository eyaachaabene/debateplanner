package com.defensemanagement.defense.defense.repository;

import com.defensemanagement.defense.defense.entity.Defense;
import com.defensemanagement.defense.defense.entity.DefenseStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface DefenseRepository extends JpaRepository<Defense, Long> {
    List<Defense> findByStatusOrderByDefenseDateAscStartTimeAsc(DefenseStatus status);

    List<Defense> findByDefenseDateOrderByStartTimeAsc(LocalDate defenseDate);

    List<Defense> findByStatusAndDefenseDateOrderByStartTimeAsc(DefenseStatus status, LocalDate defenseDate);

    List<Defense> findAllByOrderByDefenseDateAscStartTimeAsc();

    Optional<Defense> findTopByStudentIdOrderByDefenseDateDescStartTimeDesc(Long studentId);

    List<Defense> findByDefenseDate(LocalDate defenseDate);
}
