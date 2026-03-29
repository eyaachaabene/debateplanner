package com.defensemanagement.academic.student.mapper;

import com.defensemanagement.academic.student.dto.StudentRequest;
import com.defensemanagement.academic.student.dto.StudentResponse;
import com.defensemanagement.academic.student.entity.Student;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface StudentMapper {
    StudentResponse toResponse(Student student);

    Student toEntity(StudentRequest studentRequest);

    void updateEntityFromRequest(StudentRequest studentRequest, @MappingTarget Student student);
}
