package com.defensemanagement.defense.defense.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
public class CheckConflictsRequest {
    @NotNull(message = "Defense date is required")
    private LocalDate defenseDate;

    @NotNull(message = "Start time is required")
    private LocalTime startTime;

    @NotNull(message = "End time is required")
    private LocalTime endTime;

    @NotNull(message = "Student id is required")
    private Long studentId;

    @NotNull(message = "Supervisor id is required")
    private Long supervisorId;

    @NotNull(message = "President id is required")
    private Long presidentId;

    @NotNull(message = "Reviewer id is required")
    private Long reviewerId;

    @NotNull(message = "Examiner id is required")
    private Long examinerId;

    @NotNull(message = "Room id is required")
    private Long roomId;

    private Long excludeDefenseId;
}
