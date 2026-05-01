package com.defensemanagement.defense.report.repository;

import com.defensemanagement.defense.report.entity.DefenseReport;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface DefenseReportRepository extends JpaRepository<DefenseReport, Long> {
    Optional<DefenseReport> findByDefenseId(Long defenseId);
    boolean existsByDefenseId(Long defenseId);
}
