package com.defensemanagement.academic.student.repository;

import com.defensemanagement.academic.student.entity.EMajor;
import com.defensemanagement.academic.student.entity.Student;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface StudentRepository extends JpaRepository<Student, Long> {
    Optional<Student> findByUserId(Long userId);

    Optional<Student> findByEmail(String email);

    boolean existsByEmail(String email);

    boolean existsByUserId(Long userId);

    List<Student> findByMajor(EMajor major);

    List<Student> findByLevel(int level);

    List<Student> findByMajorAndLevel(EMajor major, int level);
}
