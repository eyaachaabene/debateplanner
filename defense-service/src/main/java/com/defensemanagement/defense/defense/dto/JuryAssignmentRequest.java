package com.defensemanagement.defense.defense.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class JuryAssignmentRequest {
    @NotNull(message = "Supervisor id is required")
    private Long supervisorId;

    @NotNull(message = "President id is required")
    private Long presidentId;

    @NotNull(message = "Reviewer id is required")
    private Long reviewerId;

    @NotNull(message = "Examiner id is required")
    private Long examinerId;
}
