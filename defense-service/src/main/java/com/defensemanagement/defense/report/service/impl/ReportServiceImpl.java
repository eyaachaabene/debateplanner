package com.defensemanagement.defense.report.service.impl;

import com.defensemanagement.defense.defense.dto.DefenseRequestContext;
import com.defensemanagement.defense.defense.entity.Defense;
import com.defensemanagement.defense.defense.entity.DefenseStatus;
import com.defensemanagement.defense.defense.repository.DefenseRepository;
import com.defensemanagement.defense.exception.ResourceNotFoundException;
import com.defensemanagement.defense.report.dto.ReportResponse;
import com.defensemanagement.defense.report.entity.DefenseReport;
import com.defensemanagement.defense.report.repository.DefenseReportRepository;
import com.defensemanagement.defense.report.service.ReportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service @RequiredArgsConstructor @Slf4j @Transactional
public class ReportServiceImpl implements ReportService {

    private static final List<String> ALLOWED_CONTENT_TYPES = List.of(
        "application/pdf",
        "application/msword",
        "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
    );
    private static final long MAX_FILE_SIZE_BYTES = 50L * 1024 * 1024;

    private final DefenseReportRepository reportRepository;
    private final DefenseRepository defenseRepository;

    @Value("${report.upload.dir:uploads/reports}")
    private String uploadDir;

    @Override
    public ReportResponse upload(Long defenseId, MultipartFile file, DefenseRequestContext ctx) {
        Defense defense = getDefenseOrThrow(defenseId);
        assertUploadAllowed(defense, ctx);
        validateFile(file);

        reportRepository.findByDefenseId(defenseId).ifPresent(existing -> {
            deleteFileQuietly(existing.getStoredPath());
            reportRepository.delete(existing);
        });

        String storedPath = storeFile(file, defenseId);

        DefenseReport report = DefenseReport.builder()
            .defenseId(defenseId)
            .originalFilename(file.getOriginalFilename())
            .storedPath(storedPath)
            .contentType(file.getContentType())
            .fileSize(file.getSize())
            .uploadedAt(LocalDateTime.now())
            .uploadedBy(ctx.getUserId())
            .build();

        return toResponse(reportRepository.save(report));
    }

    @Override @Transactional(readOnly = true)
    public ReportResponse getMetadata(Long defenseId) {
        return toResponse(getReportOrThrow(defenseId));
    }

    @Override @Transactional(readOnly = true)
    public String getStoredPath(Long defenseId) {
        return getReportOrThrow(defenseId).getStoredPath();
    }

    @Override
    public void delete(Long defenseId, DefenseRequestContext ctx) {
        DefenseReport report = getReportOrThrow(defenseId);
        if (!report.getUploadedBy().equals(ctx.getUserId()))
            throw new IllegalArgumentException("You can only delete your own report");
        deleteFileQuietly(report.getStoredPath());
        reportRepository.delete(report);
    }

    private Defense getDefenseOrThrow(Long defenseId) {
        return defenseRepository.findById(defenseId)
            .orElseThrow(() -> new ResourceNotFoundException("Defense not found with id: " + defenseId));
    }

    private DefenseReport getReportOrThrow(Long defenseId) {
        return reportRepository.findByDefenseId(defenseId)
            .orElseThrow(() -> new ResourceNotFoundException("No report found for defense id: " + defenseId));
    }

    private void assertUploadAllowed(Defense defense, DefenseRequestContext ctx) {
        if (defense.getStatus() == DefenseStatus.PUBLISHED)
            throw new IllegalArgumentException("Cannot upload a report for a published defense");
        if (ctx.getUserId() == null)
            throw new IllegalArgumentException("User identity could not be resolved");
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty())
            throw new IllegalArgumentException("File must not be empty");
        if (file.getSize() > MAX_FILE_SIZE_BYTES)
            throw new IllegalArgumentException("File size exceeds the 50 MB limit");
        String ct = file.getContentType();
        if (ct == null || !ALLOWED_CONTENT_TYPES.contains(ct))
            throw new IllegalArgumentException("Only PDF and Word documents are accepted");
    }

    private String storeFile(MultipartFile file, Long defenseId) {
        try {
            Path dir = Paths.get(uploadDir, String.valueOf(defenseId));
            Files.createDirectories(dir);
            String extension = getExtension(file.getOriginalFilename());
            String filename = UUID.randomUUID() + extension;
            Path target = dir.resolve(filename);
            Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
            return target.toAbsolutePath().toString();
        } catch (IOException e) {
            log.error("Failed to store report for defense {}", defenseId, e);
            throw new RuntimeException("Could not store the file. Please try again.", e);
        }
    }

    private void deleteFileQuietly(String path) {
        try { Files.deleteIfExists(Paths.get(path)); }
        catch (IOException e) { log.warn("Could not delete report file at {}: {}", path, e.getMessage()); }
    }

    private String getExtension(String filename) {
        if (filename == null || !filename.contains(".")) return "";
        return filename.substring(filename.lastIndexOf('.'));
    }

    private ReportResponse toResponse(DefenseReport r) {
        return ReportResponse.builder()
            .id(r.getId()).defenseId(r.getDefenseId())
            .originalFilename(r.getOriginalFilename()).contentType(r.getContentType())
            .fileSize(r.getFileSize()).uploadedAt(r.getUploadedAt()).uploadedBy(r.getUploadedBy())
            .build();
    }
}
