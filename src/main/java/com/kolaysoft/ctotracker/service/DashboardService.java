package com.kolaysoft.ctotracker.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kolaysoft.ctotracker.dto.DashboardProjectSummary;
import com.kolaysoft.ctotracker.dto.DashboardSummaryResponse;
import com.kolaysoft.ctotracker.entity.Project;
import com.kolaysoft.ctotracker.entity.RiskLevel;
import com.kolaysoft.ctotracker.entity.User;
import com.kolaysoft.ctotracker.entity.WeeklyReport;
import com.kolaysoft.ctotracker.entity.WorkItemStatus;
import com.kolaysoft.ctotracker.repository.ProjectRepository;
import com.kolaysoft.ctotracker.repository.WeeklyReportRepository;
import com.kolaysoft.ctotracker.repository.WorkItemRepository;

/**
 * CTO dashboard toplulastirma mantigi (on analiz H-06).
 *
 * <p>Her proje icin en guncel haftalik rapor (en yuksek hafta numarasi) bulunur; ilerleme,
 * durum, risk ve canli task bilgisi bu rapordan turer. Raporu olmayan projeler de listelenir
 * (ilerleme bilgisi bos/varsayilan). Opsiyonel risk seviyesi filtresi uygulanabilir.
 */
@Service
@Transactional(readOnly = true)
public class DashboardService {

    private final ProjectRepository projectRepository;
    private final WeeklyReportRepository weeklyReportRepository;
    private final WorkItemRepository workItemRepository;

    public DashboardService(ProjectRepository projectRepository,
                            WeeklyReportRepository weeklyReportRepository,
                            WorkItemRepository workItemRepository) {
        this.projectRepository = projectRepository;
        this.weeklyReportRepository = weeklyReportRepository;
        this.workItemRepository = workItemRepository;
    }

    /**
     * Portfoy ozetini uretir.
     *
     * @param riskFilter yalnizca bu risk seviyesindeki projeler; null ise tum projeler.
     *                   Raporu olmayan projeler risk filtresi verildiginde listelenmez.
     */
    public DashboardSummaryResponse getSummary(RiskLevel riskFilter) {
        List<DashboardProjectSummary> summaries = projectRepository.findAll().stream()
                .map(this::toSummary)
                .filter(s -> riskFilter == null || riskFilter.equals(s.riskLevel()))
                .toList();

        long highRisk = summaries.stream().filter(s -> RiskLevel.YUKSEK.equals(s.riskLevel())).count();
        long withoutReport = summaries.stream().filter(s -> !s.hasReport()).count();
        long totalLiveTasks = summaries.stream().mapToLong(DashboardProjectSummary::liveTaskCount).sum();

        return new DashboardSummaryResponse(
                summaries.size(), highRisk, withoutReport, totalLiveTasks, summaries);
    }

    private DashboardProjectSummary toSummary(Project project) {
        User owner = project.getOwner();
        String ownerName = owner != null ? owner.getFullName() : null;

        Optional<WeeklyReport> lastReport =
                weeklyReportRepository.findTopByProjectIdOrderByWeekNumberDesc(project.getId());

        if (lastReport.isEmpty()) {
            return new DashboardProjectSummary(
                    project.getId(), project.getName(), project.getCustomer(), ownerName,
                    project.getStatus(), false, null, null, 0, null, null, 0);
        }

        WeeklyReport r = lastReport.get();
        long liveTaskCount = workItemRepository.countByWeeklyReportIdAndStatus(
                r.getId(), WorkItemStatus.DEVAM_EDIYOR);

        return new DashboardProjectSummary(
                project.getId(), project.getName(), project.getCustomer(), ownerName,
                project.getStatus(), true, r.getWeekNumber(),
                r.getProgressStage(), r.getProgressStage().getPercentage(),
                r.getOverallStatus(), r.getRiskLevel(), liveTaskCount);
    }
}
