package com.defense.orchestrator.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class JuryAssignmentRequest {
    @NotNull
    private Long supervisorId;

    @NotNull
    private Long presidentId;

    @NotNull
    private Long reviewerId;

    @NotNull
    private Long examinerId;
}
