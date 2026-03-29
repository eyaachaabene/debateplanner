package com.defensemanagement.academic.professor.controller;

import com.defensemanagement.academic.professor.dto.ProfessorRequest;
import com.defensemanagement.academic.professor.dto.ProfessorResponse;
import com.defensemanagement.academic.professor.service.ProfessorService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/professors")
@RequiredArgsConstructor
public class ProfessorController {
    private final ProfessorService professorService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProfessorResponse> create(@Valid @RequestBody ProfessorRequest request) {
        ProfessorResponse response = professorService.create(request);
        return ResponseEntity.status(201).body(response);
    }

    @GetMapping("/{id}")
    @PreAuthorize("authenticated")
    public ResponseEntity<ProfessorResponse> getById(@PathVariable Long id) {
        ProfessorResponse response = professorService.getById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/user/{userId}")
    @PreAuthorize("authenticated")
    public ResponseEntity<ProfessorResponse> getByUserId(@PathVariable Long userId) {
        ProfessorResponse response = professorService.getByUserId(userId);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    @PreAuthorize("authenticated")
    public ResponseEntity<List<ProfessorResponse>> getAll() {
        List<ProfessorResponse> responses = professorService.getAll();
        return ResponseEntity.ok(responses);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProfessorResponse> update(@PathVariable Long id, @Valid @RequestBody ProfessorRequest request) {
        ProfessorResponse response = professorService.update(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        professorService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
