package com.defense.orchestrator.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class GradeRequest {
    @NotNull
    @Min(0)
    @Max(20)
    private Double grade;
}
