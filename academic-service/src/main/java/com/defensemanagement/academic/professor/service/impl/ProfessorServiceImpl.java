package com.defensemanagement.academic.professor.service.impl;

import com.defensemanagement.academic.exception.ResourceNotFoundException;
import com.defensemanagement.academic.professor.dto.ProfessorRequest;
import com.defensemanagement.academic.professor.dto.ProfessorResponse;
import com.defensemanagement.academic.professor.entity.Professor;
import com.defensemanagement.academic.professor.mapper.ProfessorMapper;
import com.defensemanagement.academic.professor.repository.ProfessorRepository;
import com.defensemanagement.academic.professor.service.ProfessorService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ProfessorServiceImpl implements ProfessorService {
    private final ProfessorRepository professorRepository;
    private final ProfessorMapper professorMapper;

    @Override
    public ProfessorResponse create(ProfessorRequest request) {
        if (professorRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email already exists: " + request.getEmail());
        }

        Professor professor = professorMapper.toEntity(request);
        Professor savedProfessor = professorRepository.save(professor);
        return professorMapper.toResponse(savedProfessor);
    }

    @Override
    public ProfessorResponse getById(Long id) {
        Professor professor = professorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Professor not found with id: " + id));
        return professorMapper.toResponse(professor);
    }

    @Override
    public ProfessorResponse getByUserId(Long userId) {
        Professor professor = professorRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Professor not found with userId: " + userId));
        return professorMapper.toResponse(professor);
    }

    @Override
    public List<ProfessorResponse> getAll() {
        return professorRepository.findAll().stream()
                .map(professorMapper::toResponse)
                .toList();
    }

    @Override
    public ProfessorResponse update(Long id, ProfessorRequest request) {
        Professor professor = professorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Professor not found with id: " + id));

        professorMapper.updateEntityFromRequest(request, professor);
        Professor updatedProfessor = professorRepository.save(professor);
        return professorMapper.toResponse(updatedProfessor);
    }

    @Override
    public void delete(Long id) {
        Professor professor = professorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Professor not found with id: " + id));
        professorRepository.delete(professor);
    }
}
