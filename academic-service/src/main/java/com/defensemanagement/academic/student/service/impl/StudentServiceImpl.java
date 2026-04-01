package com.defensemanagement.academic.student.service.impl;

import com.defensemanagement.academic.exception.ResourceNotFoundException;
import com.defensemanagement.academic.client.AuthServiceClient;
import com.defensemanagement.academic.student.dto.StudentRequest;
import com.defensemanagement.academic.student.dto.StudentResponse;
import com.defensemanagement.academic.student.entity.EMajor;
import com.defensemanagement.academic.student.entity.Student;
import com.defensemanagement.academic.student.mapper.StudentMapper;
import com.defensemanagement.academic.student.repository.StudentRepository;
import com.defensemanagement.academic.student.service.StudentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class StudentServiceImpl implements StudentService {
    private final StudentRepository studentRepository;
    private final StudentMapper studentMapper;
    private final AuthServiceClient authServiceClient;

    @Override
    public StudentResponse create(StudentRequest request) {
        if (studentRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email already exists: " + request.getEmail());
        }

        Long userId = authServiceClient.registerUser(
                request.getEmail(),
                "ChangeMe123!",
                "STUDENT"
        );

        Student student = studentMapper.toEntity(request);
        student.setUserId(userId);
        Student savedStudent = studentRepository.save(student);
        return studentMapper.toResponse(savedStudent);
    }

    @Override
    public StudentResponse getById(Long id) {
        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with id: " + id));
        return studentMapper.toResponse(student);
    }

    @Override
    public StudentResponse getByUserId(Long userId) {
        Student student = studentRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with userId: " + userId));
        return studentMapper.toResponse(student);
    }

    @Override
    public StudentResponse getCurrentStudent(Long userId) {
        return getByUserId(userId);
    }

    @Override
    public List<StudentResponse> getAll() {
        return studentRepository.findAll().stream()
                .map(studentMapper::toResponse)
                .toList();
    }

    @Override
    public List<StudentResponse> getByMajor(EMajor major) {
        return studentRepository.findByMajor(major).stream()
                .map(studentMapper::toResponse)
                .toList();
    }

    @Override
    public List<StudentResponse> getByLevel(int level) {
        return studentRepository.findByLevel(level).stream()
                .map(studentMapper::toResponse)
                .toList();
    }

    @Override
    public List<StudentResponse> getByMajorAndLevel(EMajor major, int level) {
        return studentRepository.findByMajorAndLevel(major, level).stream()
                .map(studentMapper::toResponse)
                .toList();
    }

    @Override
    public StudentResponse update(Long id, StudentRequest request) {
        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with id: " + id));

        studentMapper.updateEntityFromRequest(request, student);
        Student updatedStudent = studentRepository.save(student);
        return studentMapper.toResponse(updatedStudent);
    }

    @Override
    public void delete(Long id) {
        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with id: " + id));
        studentRepository.delete(student);
    }
}
