package com.defensemanagement.defense.report.dto;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data @Builder
public class ReportResponse {
    private Long id;
    private Long defenseId;
    private String originalFilename;
    private String contentType;
    private Long fileSize;
    private LocalDateTime uploadedAt;
    private String uploadedBy;
}
