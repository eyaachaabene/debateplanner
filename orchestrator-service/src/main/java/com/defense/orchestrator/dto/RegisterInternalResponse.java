package com.defense.orchestrator.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class RegisterInternalResponse {
    @JsonProperty("userId")
    private Long userId;

    @JsonCreator
    public RegisterInternalResponse(@JsonProperty("userId") Long userId) {
        this.userId = userId;
    }
}
