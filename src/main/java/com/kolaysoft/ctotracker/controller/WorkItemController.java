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

import com.kolaysoft.ctotracker.dto.WorkItemRequest;
import com.kolaysoft.ctotracker.dto.WorkItemResponse;
import com.kolaysoft.ctotracker.service.WorkItemService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

/**
 * İş kalemi endpoint'leri. Olusturma/listeleme rapor altindan
 * ({@code /api/reports/{reportId}/work-items}); tekil islemler is kalemi id'si uzerinden
 * ({@code /api/work-items/{id}}) yapilir (on analiz, bolum 5).
 */
@Tag(name = "WorkItem", description = "İş kalemi yönetimi")
@RestController
public class WorkItemController {

    private final WorkItemService workItemService;

    public WorkItemController(WorkItemService workItemService) {
        this.workItemService = workItemService;
    }

    @Operation(summary = "Rapora ait iş kalemlerini listele")
    @GetMapping(value = "/api/reports/{reportId}/work-items", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<WorkItemResponse> listByReport(@PathVariable Long reportId) {
        return workItemService.findByReport(reportId);
    }

    @Operation(summary = "Rapora iş kalemi oluştur")
    @PostMapping(value = "/api/reports/{reportId}/work-items",
            consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<WorkItemResponse> create(@PathVariable Long reportId,
                                                   @Valid @RequestBody WorkItemRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(workItemService.create(reportId, request));
    }

    @Operation(summary = "İş kalemi detayı")
    @GetMapping(value = "/api/work-items/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public WorkItemResponse get(@PathVariable Long id) {
        return workItemService.findById(id);
    }

    @Operation(summary = "İş kalemi güncelle")
    @PutMapping(value = "/api/work-items/{id}",
            consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public WorkItemResponse update(@PathVariable Long id, @Valid @RequestBody WorkItemRequest request) {
        return workItemService.update(id, request);
    }

    @Operation(summary = "İş kalemi sil")
    @DeleteMapping("/api/work-items/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        workItemService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
