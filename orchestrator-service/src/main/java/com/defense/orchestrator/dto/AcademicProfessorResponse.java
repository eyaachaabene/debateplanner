package com.defense.orchestrator.dto;

import lombok.Data;

@Data
public class AcademicProfessorResponse {
    private Long id;
    private Long userId;
    private String firstName;
    private String lastName;
    private String email;
}
