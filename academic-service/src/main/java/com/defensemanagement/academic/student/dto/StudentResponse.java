package com.defensemanagement.academic.student.dto;

import com.defensemanagement.academic.student.entity.EMajor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class StudentResponse {
    private Long id;
    private Long userId;
    private String firstName;
    private String lastName;
    private String email;
    private EMajor major;
    private int level;
}
