package com.kolaysoft.ctotracker.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.kolaysoft.ctotracker.dto.WeeklyReportRequest;
import com.kolaysoft.ctotracker.dto.WeeklyReportResponse;
import com.kolaysoft.ctotracker.service.WeeklyReportService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

/**
 * Haftalik rapor endpoint'leri. Olusturma/listeleme proje altindan
 * ({@code /api/projects/{projectId}/reports}); tekil islemler rapor id'si uzerinden
 * ({@code /api/reports/{id}}) yapilir (on analiz, bolum 5).
 */
@Tag(name = "WeeklyReport", description = "Haftalik rapor yonetimi")
@RestController
public class WeeklyReportController {

    private final WeeklyReportService weeklyReportService;

    public WeeklyReportController(WeeklyReportService weeklyReportService) {
        this.weeklyReportService = weeklyReportService;
    }

    @Operation(summary = "Projeye ait haftalik raporlari listele")
    @GetMapping(value = "/api/projects/{projectId}/reports", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<WeeklyReportResponse> listByProject(@PathVariable Long projectId) {
        return weeklyReportService.findByProject(projectId);
    }

    @Operation(summary = "Projeye haftalik rapor olustur")
    @PostMapping(value = "/api/projects/{projectId}/reports",
            consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<WeeklyReportResponse> create(@PathVariable Long projectId,
                                                       @Valid @RequestBody WeeklyReportRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(weeklyReportService.create(projectId, request));
    }

    @Operation(summary = "Haftalik rapor detayi")
    @GetMapping(value = "/api/reports/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public WeeklyReportResponse get(@PathVariable Long id) {
        return weeklyReportService.findById(id);
    }

    @Operation(summary = "Haftalik rapor guncelle")
    @PutMapping(value = "/api/reports/{id}",
            consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public WeeklyReportResponse update(@PathVariable Long id, @Valid @RequestBody WeeklyReportRequest request) {
        return weeklyReportService.update(id, request);
    }

    @Operation(summary = "Haftalik rapor sil")
    @DeleteMapping("/api/reports/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        weeklyReportService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
