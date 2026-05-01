package com.defensemanagement.defense.report.controller;

import com.defensemanagement.defense.defense.dto.DefenseRequestContext;
import com.defensemanagement.defense.report.dto.ReportResponse;
import com.defensemanagement.defense.report.service.ReportService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.PathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.nio.file.Paths;

@RestController
@RequestMapping("/api/v1/defenses/{defenseId}/report")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAuthority('ROLE_STUDENT')")
    public ResponseEntity<ReportResponse> upload(
            @PathVariable Long defenseId,
            @RequestParam("file") MultipartFile file,
            HttpServletRequest servletRequest) {
        return ResponseEntity.status(201).body(
            reportService.upload(defenseId, file, buildContext(servletRequest)));
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ReportResponse> getMetadata(@PathVariable Long defenseId) {
        return ResponseEntity.ok(reportService.getMetadata(defenseId));
    }

    @GetMapping("/download")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Resource> download(@PathVariable Long defenseId) {
        String path = reportService.getStoredPath(defenseId);
        Resource resource = new PathResource(Paths.get(path));
        if (!resource.exists()) return ResponseEntity.notFound().build();
        ReportResponse meta = reportService.getMetadata(defenseId);
        return ResponseEntity.ok()
            .contentType(MediaType.parseMediaType(meta.getContentType()))
            .header(HttpHeaders.CONTENT_DISPOSITION,
                "attachment; filename=\"" + meta.getOriginalFilename() + "\"")
            .body(resource);
    }

    @DeleteMapping
    @PreAuthorize("hasAuthority('ROLE_STUDENT')")
    public ResponseEntity<Void> delete(
            @PathVariable Long defenseId,
            HttpServletRequest servletRequest) {
        reportService.delete(defenseId, buildContext(servletRequest));
        return ResponseEntity.noContent().build();
    }

    private DefenseRequestContext buildContext(HttpServletRequest request) {
        return DefenseRequestContext.builder()
            .userId(request.getHeader("X-User-Id"))
            .username(request.getHeader("X-User-Username"))
            .roles(request.getHeader("X-User-Roles"))
            .requestId(request.getHeader("X-Request-Id"))
            .build();
    }
}
