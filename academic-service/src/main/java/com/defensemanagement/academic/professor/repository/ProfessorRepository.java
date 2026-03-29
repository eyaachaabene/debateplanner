package com.defensemanagement.academic.professor.repository;

import com.defensemanagement.academic.professor.entity.Professor;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProfessorRepository extends JpaRepository<Professor, Long> {
    Optional<Professor> findByUserId(Long userId);

    Optional<Professor> findByEmail(String email);

    boolean existsByEmail(String email);

    boolean existsByUserId(Long userId);
}
