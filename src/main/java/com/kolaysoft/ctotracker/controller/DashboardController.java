package com.kolaysoft.ctotracker.controller;

import java.util.List;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.kolaysoft.ctotracker.dto.DashboardSummaryResponse;
import com.kolaysoft.ctotracker.dto.filter.DashboardFilter;
import com.kolaysoft.ctotracker.entity.OverallStatus;
import com.kolaysoft.ctotracker.entity.ProjectStatus;
import com.kolaysoft.ctotracker.entity.RiskLevel;
import com.kolaysoft.ctotracker.service.DashboardService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

/**
 * CTO dashboard endpoint'i (on analiz H-06). Tum projelerin son durumunu tek ozet olarak doner.
 *
 * <p>Filtre parametreleri T13 filtre sozlesmesinin 2. bolumune gore tanimlidir. Parametreler tek tek
 * {@code @RequestParam} olarak alinir (sozlesme S-01): boylece tip hatalari mevcut
 * {@code 400 INVALID_REQUEST} yolundan, aralik hatalari {@code 400 VALIDATION_ERROR} yolundan doner.
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
            description = "Tum projelerin degerlendirilen haftalik rapora gore durumunu, ilerlemesini, riskini, "
                    + "canli task sayisini ve gecikme bilgisini tek ozette doner. Farkli filtreler VE ile, "
                    + "cok degerli bir parametrenin degerleri VEYA ile birlesir (or. riskLevel=ORTA,YUKSEK). "
                    + "Sayaclar filtrelenmis kumeden hesaplanir.")
    @GetMapping("/summary")
    public DashboardSummaryResponse summary(
            @Parameter(description = "Yalnizca bu proje", example = "1")
            @RequestParam(required = false) Long projectId,

            @Parameter(description = "Proje durumu; birden cok deger virgulle verilebilir", example = "DEVAM_EDIYOR")
            @RequestParam(required = false) List<ProjectStatus> projectStatus,

            @Parameter(description = "Sorumlu proje yoneticisinin kullanici id'si", example = "1")
            @RequestParam(required = false) Long ownerId,

            @Parameter(description = "Referans hafta (1-53): her proje icin bu haftaya kadarki en son rapor esas "
                    + "alinir; o haftaya kadar raporu olmayan projeler hasReport=false ile listelenir", example = "29")
            @RequestParam(required = false)
            @Min(value = 1, message = "Hafta numarasi en az 1 olmalidir.")
            @Max(value = 53, message = "Hafta numarasi en fazla 53 olmalidir.") Integer weekNumber,

            @Parameter(description = "Raporun genel durumu; birden cok deger virgulle verilebilir", example = "RISKLI")
            @RequestParam(required = false) List<OverallStatus> overallStatus,

            @Parameter(description = "Raporun risk seviyesi; birden cok deger virgulle verilebilir",
                    example = "ORTA,YUKSEK")
            @RequestParam(required = false) List<RiskLevel> riskLevel,

            @Parameter(description = "true: yalnizca gecikmedeki projeler, false: yalnizca gecikmede olmayanlar",
                    example = "true")
            @RequestParam(required = false) Boolean delayed) {

        return dashboardService.getSummary(new DashboardFilter(
                projectId, projectStatus, ownerId, weekNumber, overallStatus, riskLevel, delayed));
    }
}
