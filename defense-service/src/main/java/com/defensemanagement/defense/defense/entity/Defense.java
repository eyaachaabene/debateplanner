package com.defensemanagement.defense.defense.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "defenses")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Defense {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 255)
    private String projectTitle;

    @Column(nullable = false)
    private LocalDate defenseDate;

    @Column(nullable = false)
    private LocalTime startTime;

    @Column(nullable = false)
    private LocalTime endTime;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private DefenseStatus status;

    private Double finalAverage;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private Mention mention;

    private Double presidentGrade;
    private Double reviewerGrade;
    private Double examinerGrade;
    private Double supervisorGrade;

    @Column(nullable = false)
    private Long studentId;

    @Column(nullable = false)
    private Long supervisorId;

    @Column(nullable = false)
    private Long presidentId;

    @Column(nullable = false)
    private Long reviewerId;

    @Column(nullable = false)
    private Long examinerId;

    @Column(nullable = false)
    private Long roomId;
}
