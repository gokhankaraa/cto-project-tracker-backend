package com.kolaysoft.ctotracker.dto.filter;

import java.util.List;

import com.kolaysoft.ctotracker.entity.OverallStatus;
import com.kolaysoft.ctotracker.entity.ProjectStatus;
import com.kolaysoft.ctotracker.entity.RiskLevel;

/**
 * CTO dashboard filtresi (filtre sözleşmesi T13, bölüm 2).
 *
 * <p>Tekil değerlerde null, çok değerli alanlarda boş liste "filtre yok" demektir.
 * {@code weekNumber} bir filtre değil, değerlendirilen raporu seçen referans haftadır (K-T13-05).
 */
public record DashboardFilter(
        Long projectId,
        List<ProjectStatus> projectStatuses,
        Long ownerId,
        Integer weekNumber,
        List<OverallStatus> overallStatuses,
        List<RiskLevel> riskLevels,
        Boolean delayed) {

    public DashboardFilter {
        projectStatuses = FilterValues.values(projectStatuses);
        overallStatuses = FilterValues.values(overallStatuses);
        riskLevels = FilterValues.values(riskLevels);
    }

    /** Hiç filtre içermeyen dashboard isteği (MVP'deki davranış). */
    public static DashboardFilter none() {
        return new DashboardFilter(null, null, null, null, null, null, null);
    }

    /**
     * Değerlendirilen rapora dayalı bir koşul (rapor durumu, risk, gecikme) var mı.
     * Varsa raporu olmayan projeler listelenmez (K-T13-04).
     */
    public boolean hasReportCriteria() {
        return !overallStatuses.isEmpty() || !riskLevels.isEmpty() || delayed != null;
    }
}
