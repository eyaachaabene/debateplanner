package com.defensemanagement.defense.defense.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class ConflictCheckResponse {
    private boolean hasConflicts;
    private List<String> errors;
}
