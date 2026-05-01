package com.defense.orchestrator.dto;

import lombok.Data;

@Data
public class AcademicStudentRequest {
    private Long userId;
    private String firstName;
    private String lastName;
    private String email;
    private EMajor major;
    private int level;
}