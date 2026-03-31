package com.defensemanagement.defense.defense.mapper;

import com.defensemanagement.defense.defense.dto.DefenseRequest;
import com.defensemanagement.defense.defense.dto.DefenseResponse;
import com.defensemanagement.defense.defense.entity.Defense;
import com.defensemanagement.defense.defense.entity.DefenseStatus;
import org.springframework.stereotype.Component;

@Component
public class DefenseMapper {

    public Defense toEntity(DefenseRequest request) {
        return Defense.builder()
                .projectTitle(request.getProjectTitle())
                .defenseDate(request.getDefenseDate())
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .status(DefenseStatus.PLANNED)
                .studentId(request.getStudentId())
                .supervisorId(request.getSupervisorId())
                .presidentId(request.getPresidentId())
                .reviewerId(request.getReviewerId())
                .examinerId(request.getExaminerId())
                .roomId(request.getRoomId())
                .build();
    }

    public void updateEntityFromRequest(DefenseRequest request, Defense defense) {
        defense.setProjectTitle(request.getProjectTitle());
        defense.setDefenseDate(request.getDefenseDate());
        defense.setStartTime(request.getStartTime());
        defense.setEndTime(request.getEndTime());
        defense.setStudentId(request.getStudentId());
        defense.setSupervisorId(request.getSupervisorId());
        defense.setPresidentId(request.getPresidentId());
        defense.setReviewerId(request.getReviewerId());
        defense.setExaminerId(request.getExaminerId());
        defense.setRoomId(request.getRoomId());
    }

    public DefenseResponse toResponse(Defense defense) {
        return DefenseResponse.builder()
                .id(defense.getId())
                .projectTitle(defense.getProjectTitle())
                .defenseDate(defense.getDefenseDate())
                .startTime(defense.getStartTime())
                .endTime(defense.getEndTime())
                .status(com.defensemanagement.defense.defense.dto.DefenseStatus.valueOf(defense.getStatus().name()))
                .finalAverage(defense.getFinalAverage())
                .mention(defense.getMention())
                .presidentGrade(defense.getPresidentGrade())
                .reviewerGrade(defense.getReviewerGrade())
                .examinerGrade(defense.getExaminerGrade())
                .supervisorGrade(defense.getSupervisorGrade())
                .studentId(defense.getStudentId())
                .supervisorId(defense.getSupervisorId())
                .presidentId(defense.getPresidentId())
                .reviewerId(defense.getReviewerId())
                .examinerId(defense.getExaminerId())
                .roomId(defense.getRoomId())
                .build();
    }
}
