package com.defense.orchestrator.dto;

import com.defense.orchestrator.dto.Mention;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
@Builder
public class DefenseResponse {
    private Long id;
    private String projectTitle;
    private LocalDate defenseDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private String status;
    private Double finalAverage;
    private Mention mention;
    private Double presidentGrade;
    private Double reviewerGrade;
    private Double examinerGrade;
    private Double supervisorGrade;
    private Long studentId;
    private Long supervisorId;
    private Long presidentId;
    private Long reviewerId;
    private Long examinerId;
    private Long roomId;
}
