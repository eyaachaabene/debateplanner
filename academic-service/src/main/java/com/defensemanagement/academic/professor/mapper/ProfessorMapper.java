package com.defensemanagement.academic.professor.mapper;

import com.defensemanagement.academic.professor.dto.ProfessorRequest;
import com.defensemanagement.academic.professor.dto.ProfessorResponse;
import com.defensemanagement.academic.professor.entity.Professor;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface ProfessorMapper {
    ProfessorResponse toResponse(Professor professor);

    Professor toEntity(ProfessorRequest professorRequest);

    void updateEntityFromRequest(ProfessorRequest professorRequest, @MappingTarget Professor professor);
}
