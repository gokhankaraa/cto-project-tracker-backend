package com.kolaysoft.ctotracker.controller;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.kolaysoft.ctotracker.dto.DashboardSummaryResponse;
import com.kolaysoft.ctotracker.entity.RiskLevel;
import com.kolaysoft.ctotracker.service.DashboardService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * CTO dashboard endpoint'i (on analiz H-06). Tum projelerin son durumunu tek ozet olarak doner.
 * Gelismis filtreleme MVP disidir; yalnizca opsiyonel risk seviyesi filtresi desteklenir.
 */
@Tag(name = "Dashboard", description = "CTO portfoy ozeti")
@RestController
@RequestMapping(value = "/api/dashboard", produces = MediaType.APPLICATION_JSON_VALUE)
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @Operation(summary = "CTO portfoy ozeti",
            description = "Tum projelerin son haftalik rapor durumunu, ilerlemesini, riskini ve canli "
                    + "task sayisini tek ozette doner. Opsiyonel 'riskLevel' ile filtrelenebilir.")
    @GetMapping("/summary")
    public DashboardSummaryResponse summary(
            @RequestParam(required = false) RiskLevel riskLevel) {
        return dashboardService.getSummary(riskLevel);
    }
}
