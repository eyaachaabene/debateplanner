package com.defensemanagement.academic.student.service;

import com.defensemanagement.academic.student.dto.StudentRequest;
import com.defensemanagement.academic.student.dto.StudentResponse;
import com.defensemanagement.academic.student.entity.EMajor;

import java.util.List;

public interface StudentService {
    StudentResponse create(StudentRequest request);

    StudentResponse getById(Long id);

    StudentResponse getByUserId(Long userId);

    List<StudentResponse> getAll();

    List<StudentResponse> getByMajor(EMajor major);

    List<StudentResponse> getByLevel(int level);

    List<StudentResponse> getByMajorAndLevel(EMajor major, int level);

    StudentResponse update(Long id, StudentRequest request);

    void delete(Long id);
}
