package com.defensemanagement.academic.professor.service;

import com.defensemanagement.academic.professor.dto.ProfessorRequest;
import com.defensemanagement.academic.professor.dto.ProfessorResponse;

import java.util.List;

public interface ProfessorService {
    ProfessorResponse create(ProfessorRequest request);

    ProfessorResponse getById(Long id);

    ProfessorResponse getByUserId(Long userId);

    ProfessorResponse getCurrentProfessor(Long userId);

    List<ProfessorResponse> getAll();

    ProfessorResponse update(Long id, ProfessorRequest request);

    void delete(Long id);
}
