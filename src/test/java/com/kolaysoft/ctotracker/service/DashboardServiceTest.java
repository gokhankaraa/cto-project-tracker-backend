package com.kolaysoft.ctotracker.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.kolaysoft.ctotracker.dto.DashboardSummaryResponse;
import com.kolaysoft.ctotracker.entity.OverallStatus;
import com.kolaysoft.ctotracker.entity.ProgressStage;
import com.kolaysoft.ctotracker.entity.Project;
import com.kolaysoft.ctotracker.entity.ProjectStatus;
import com.kolaysoft.ctotracker.entity.RiskLevel;
import com.kolaysoft.ctotracker.entity.WeeklyReport;
import com.kolaysoft.ctotracker.entity.WorkItemStatus;
import com.kolaysoft.ctotracker.repository.ProjectRepository;
import com.kolaysoft.ctotracker.repository.WeeklyReportRepository;
import com.kolaysoft.ctotracker.repository.WorkItemRepository;

/** DashboardService toplulastirma testleri (Spring context'siz, Mockito ile). */
@ExtendWith(MockitoExtension.class)
class DashboardServiceTest {

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private WeeklyReportRepository weeklyReportRepository;

    @Mock
    private WorkItemRepository workItemRepository;

    @InjectMocks
    private DashboardService service;

    private Project project(Long id, String name, RiskLevel ignored) {
        Project p = new Project();
        p.setId(id);
        p.setName(name);
        p.setCustomer("Kolaysoft");
        p.setStatus(ProjectStatus.DEVAM_EDIYOR);
        return p;
    }

    private WeeklyReport report(Long id, Long projectId, int week, ProgressStage stage, RiskLevel risk) {
        Project p = new Project();
        p.setId(projectId);
        WeeklyReport r = new WeeklyReport();
        r.setId(id);
        r.setProject(p);
        r.setWeekNumber(week);
        r.setReportDate(LocalDate.of(2026, 7, 27));
        r.setProgressStage(stage);
        r.setOverallStatus(OverallStatus.YOLUNDA);
        r.setRiskLevel(risk);
        return r;
    }

    @Test
    @DisplayName("Ozet: raporu olan ve olmayan projeler birlikte, son rapor ve sayaclar dogru")
    void summaryIncludesReportedAndUnreportedProjects() {
        Project peyk = project(1L, "PEYK", null);
        Project bos = project(2L, "Raporsuz", null);
        when(projectRepository.findAll()).thenReturn(List.of(peyk, bos));

        WeeklyReport peykLast = report(10L, 1L, 30, ProgressStage.TEST, RiskLevel.YUKSEK);
        when(weeklyReportRepository.findTopByProjectIdOrderByWeekNumberDesc(1L))
                .thenReturn(Optional.of(peykLast));
        when(weeklyReportRepository.findTopByProjectIdOrderByWeekNumberDesc(2L))
                .thenReturn(Optional.empty());
        when(workItemRepository.countByWeeklyReportIdAndStatus(10L, WorkItemStatus.DEVAM_EDIYOR))
                .thenReturn(2L);

        DashboardSummaryResponse summary = service.getSummary(null);

        assertThat(summary.totalProjects()).isEqualTo(2);
        assertThat(summary.highRiskProjects()).isEqualTo(1);
        assertThat(summary.projectsWithoutReport()).isEqualTo(1);
        assertThat(summary.totalLiveTasks()).isEqualTo(2);

        var peykSummary = summary.projects().stream().filter(s -> s.projectId().equals(1L)).findFirst().orElseThrow();
        assertThat(peykSummary.hasReport()).isTrue();
        assertThat(peykSummary.lastWeekNumber()).isEqualTo(30);
        assertThat(peykSummary.progressStage()).isEqualTo(ProgressStage.TEST);
        assertThat(peykSummary.progressPercentage()).isEqualTo(75);
        assertThat(peykSummary.liveTaskCount()).isEqualTo(2);

        var bosSummary = summary.projects().stream().filter(s -> s.projectId().equals(2L)).findFirst().orElseThrow();
        assertThat(bosSummary.hasReport()).isFalse();
        assertThat(bosSummary.progressStage()).isNull();
        assertThat(bosSummary.progressPercentage()).isZero();
        assertThat(bosSummary.liveTaskCount()).isZero();
    }

    @Test
    @DisplayName("Risk filtresi: yalnizca istenen risk seviyesindeki projeler doner")
    void riskFilterReturnsOnlyMatchingProjects() {
        Project peyk = project(1L, "PEYK", null);
        Project edonusum = project(2L, "e-Donusum", null);
        when(projectRepository.findAll()).thenReturn(List.of(peyk, edonusum));

        WeeklyReport peykLast = report(10L, 1L, 30, ProgressStage.TEST, RiskLevel.DUSUK);
        WeeklyReport edonusumLast = report(20L, 2L, 30, ProgressStage.ANALIZ, RiskLevel.YUKSEK);
        when(weeklyReportRepository.findTopByProjectIdOrderByWeekNumberDesc(1L))
                .thenReturn(Optional.of(peykLast));
        when(weeklyReportRepository.findTopByProjectIdOrderByWeekNumberDesc(2L))
                .thenReturn(Optional.of(edonusumLast));
        lenient().when(workItemRepository.countByWeeklyReportIdAndStatus(any(), eq(WorkItemStatus.DEVAM_EDIYOR)))
                .thenReturn(0L);

        DashboardSummaryResponse summary = service.getSummary(RiskLevel.YUKSEK);

        assertThat(summary.projects()).hasSize(1);
        assertThat(summary.projects().get(0).projectId()).isEqualTo(2L);
        assertThat(summary.projects().get(0).riskLevel()).isEqualTo(RiskLevel.YUKSEK);
    }

    @Test
    @DisplayName("Bos portfoy: proje yoksa sayaclar sifir, liste bos")
    void emptyPortfolioReturnsZeroes() {
        when(projectRepository.findAll()).thenReturn(List.of());

        DashboardSummaryResponse summary = service.getSummary(null);

        assertThat(summary.totalProjects()).isZero();
        assertThat(summary.projects()).isEmpty();
        assertThat(summary.totalLiveTasks()).isZero();
    }
}
