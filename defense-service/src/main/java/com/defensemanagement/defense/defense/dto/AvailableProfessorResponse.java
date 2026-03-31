package com.defensemanagement.defense.defense.dto;

import lombok.Data;

@Data
public class AvailableProfessorResponse {
    private Long id;
    private Long userId;
    private String firstName;
    private String lastName;
    private String email;
}
