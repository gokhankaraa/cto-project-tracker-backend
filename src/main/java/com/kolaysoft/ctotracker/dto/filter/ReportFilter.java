package com.kolaysoft.ctotracker.dto.filter;

import java.time.LocalDate;
import java.util.List;

import com.kolaysoft.ctotracker.entity.OverallStatus;
import com.kolaysoft.ctotracker.entity.ProgressStage;
import com.kolaysoft.ctotracker.entity.RiskLevel;

/**
 * Projeler arası haftalık rapor listesi filtresi (filtre sözleşmesi T13, bölüm 3).
 *
 * <p>Tekil değerlerde null, çok değerli alanlarda boş liste "filtre yok" demektir.
 * Hafta ve tarih aralıklarının uçları dahildir.
 */
public record ReportFilter(
        Long projectId,
        Long ownerId,
        Integer weekFrom,
        Integer weekTo,
        LocalDate dateFrom,
        LocalDate dateTo,
        List<OverallStatus> overallStatuses,
        List<RiskLevel> riskLevels,
        List<ProgressStage> progressStages) {

    public ReportFilter {
        overallStatuses = FilterValues.values(overallStatuses);
        riskLevels = FilterValues.values(riskLevels);
        progressStages = FilterValues.values(progressStages);
    }
}
