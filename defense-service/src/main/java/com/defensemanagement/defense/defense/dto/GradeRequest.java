package com.defensemanagement.defense.defense.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class GradeRequest {
    @NotNull(message = "Grade is required")
    @DecimalMin(value = "0.0", message = "Grade must be greater than or equal to 0")
    @DecimalMax(value = "20.0", message = "Grade must be less than or equal to 20")
    private Double grade;
}
