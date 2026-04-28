package com.defense.orchestrator.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
public class DefenseRequest {
    @NotBlank
    @Size(max = 255)
    private String projectTitle;

    @NotNull
    private LocalDate defenseDate;

    @NotNull
    private LocalTime startTime;

    @NotNull
    private LocalTime endTime;

    @NotNull
    private Long studentId;

    @NotNull
    private Long supervisorId;

    @NotNull
    private Long presidentId;

    @NotNull
    private Long reviewerId;

    @NotNull
    private Long examinerId;

    @NotNull
    private Long roomId;
}
