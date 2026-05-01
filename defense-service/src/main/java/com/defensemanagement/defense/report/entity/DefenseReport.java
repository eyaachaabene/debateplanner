package com.defensemanagement.defense.report.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "defense_reports")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class DefenseReport {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "defense_id", nullable = false, unique = true)
    private Long defenseId;

    @Column(name = "original_filename", nullable = false)
    private String originalFilename;

    @Column(name = "stored_path", nullable = false)
    private String storedPath;

    @Column(name = "content_type", nullable = false)
    private String contentType;

    @Column(name = "file_size", nullable = false)
    private Long fileSize;

    @Column(name = "uploaded_at", nullable = false)
    private LocalDateTime uploadedAt;

    @Column(name = "uploaded_by", nullable = false)
    private String uploadedBy;
}
