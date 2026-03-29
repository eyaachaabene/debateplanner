package com.defensemanagement.academic.professor.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ProfessorResponse {
    private Long id;
    private Long userId;
    private String firstName;
    private String lastName;
    private String email;
}
