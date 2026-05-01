package com.defensemanagement.defense.report.service;

import com.defensemanagement.defense.defense.dto.DefenseRequestContext;
import com.defensemanagement.defense.report.dto.ReportResponse;
import org.springframework.web.multipart.MultipartFile;

public interface ReportService {
    ReportResponse upload(Long defenseId, MultipartFile file, DefenseRequestContext ctx);
    ReportResponse getMetadata(Long defenseId);
    String getStoredPath(Long defenseId);
    void delete(Long defenseId, DefenseRequestContext ctx);
}
