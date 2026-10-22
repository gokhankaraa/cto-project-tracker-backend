package com.kolaysoft.ctotracker.dto.filter;

import java.util.List;

import com.kolaysoft.ctotracker.entity.WorkItemStatus;

/**
 * Projeler arası iş kalemi listesi filtresi (filtre sözleşmesi T13, bölüm 4).
 *
 * <p>Liste her zaman projelerin değerlendirilen raporuna göre çalışır (S-05): {@code weekNumber}
 * yoksa en son rapor, varsa hafta numarası ≤ weekNumber olan en son rapor.
 */
public record WorkItemFilter(
        Long projectId,
        Integer weekNumber,
        String assignee,
        List<WorkItemStatus> statuses,
        Boolean delayed) {

    public WorkItemFilter {
        assignee = FilterValues.text(assignee);
        statuses = FilterValues.values(statuses);
    }
}
