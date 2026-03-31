package com.defensemanagement.defense.defense.dto;

import com.defensemanagement.defense.defense.entity.Mention;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DefenseGradesResponse {
    private Double presidentGrade;
    private Double reviewerGrade;
    private Double examinerGrade;
    private Double supervisorGrade;
    private Double finalAverage;
    private Mention mention;
}
