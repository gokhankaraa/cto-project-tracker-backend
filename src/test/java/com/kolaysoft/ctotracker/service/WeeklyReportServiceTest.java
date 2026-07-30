package com.kolaysoft.ctotracker.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.kolaysoft.ctotracker.common.exception.BusinessRuleException;
import com.kolaysoft.ctotracker.common.exception.DuplicateResourceException;
import com.kolaysoft.ctotracker.common.exception.ResourceNotFoundException;
import com.kolaysoft.ctotracker.dto.WeeklyReportRequest;
import com.kolaysoft.ctotracker.dto.WeeklyReportResponse;
import com.kolaysoft.ctotracker.entity.OverallStatus;
import com.kolaysoft.ctotracker.entity.ProgressStage;
import com.kolaysoft.ctotracker.entity.Project;
import com.kolaysoft.ctotracker.entity.RiskLevel;
import com.kolaysoft.ctotracker.entity.WeeklyReport;
import com.kolaysoft.ctotracker.repository.ProjectRepository;
import com.kolaysoft.ctotracker.repository.WeeklyReportRepository;
import com.kolaysoft.ctotracker.repository.WorkItemRepository;

/** WeeklyReportService is kurallarinin birim testleri (Spring context'siz, Mockito ile). */
@ExtendWith(MockitoExtension.class)
class WeeklyReportServiceTest {

    @Mock
    private WeeklyReportRepository weeklyReportRepository;

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private WorkItemRepository workItemRepository;

    @InjectMocks
    private WeeklyReportService service;

    private Project project() {
        Project p = new Project();
        p.setId(1L);
        p.setName("PEYK");
        return p;
    }

    private WeeklyReportRequest request(int week, ProgressStage stage) {
        return new WeeklyReportRequest(week, LocalDate.of(2026, 7, 27), stage,
                OverallStatus.YOLUNDA, RiskLevel.ORTA, "yapilanlar", "yapilacaklar", "riskler", "not");
    }

    private WeeklyReport existingReport(Long id, int week, ProgressStage stage) {
        WeeklyReport r = new WeeklyReport();
        r.setId(id);
        r.setProject(project());
        r.setWeekNumber(week);
        r.setReportDate(LocalDate.of(2026, 7, 20));
        r.setProgressStage(stage);
        r.setOverallStatus(OverallStatus.YOLUNDA);
        r.setRiskLevel(RiskLevel.ORTA);
        return r;
    }

    @Test
    @DisplayName("Olusturma: yuzde ilerleme kademesinden turer")
    void createDerivesPercentageFromStage() {
        when(projectRepository.findById(1L)).thenReturn(Optional.of(project()));
        when(weeklyReportRepository.existsByProjectIdAndWeekNumber(1L, 30)).thenReturn(false);
        when(weeklyReportRepository.save(any())).thenAnswer(inv -> {
            WeeklyReport r = inv.getArgument(0);
            r.setId(5L);
            return r;
        });

        WeeklyReportResponse response = service.create(1L, request(30, ProgressStage.GELISTIRME));

        assertThat(response.progressStage()).isEqualTo(ProgressStage.GELISTIRME);
        assertThat(response.progressPercentage()).isEqualTo(50);
        assertThat(response.projectId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("Olusturma: ayni proje+hafta icin ikinci rapor 409 DuplicateResource")
    void createRejectsDuplicateProjectWeek() {
        when(projectRepository.findById(1L)).thenReturn(Optional.of(project()));
        when(weeklyReportRepository.existsByProjectIdAndWeekNumber(1L, 30)).thenReturn(true);

        assertThatThrownBy(() -> service.create(1L, request(30, ProgressStage.ANALIZ)))
                .isInstanceOf(DuplicateResourceException.class);

        verify(weeklyReportRepository, never()).save(any());
    }

    @Test
    @DisplayName("Olusturma: olmayan proje 404 ResourceNotFound")
    void createRejectsMissingProject() {
        when(projectRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.create(99L, request(30, ProgressStage.ANALIZ)))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("Guncelleme: kademe sirasi atlanamaz (ANALIZ -> TEST) 400 BusinessRule")
    void updateRejectsStageSkip() {
        when(weeklyReportRepository.findById(5L)).thenReturn(Optional.of(existingReport(5L, 30, ProgressStage.ANALIZ)));

        assertThatThrownBy(() -> service.update(5L, request(30, ProgressStage.TEST)))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("atlanamaz");

        verify(weeklyReportRepository, never()).save(any());
    }

    @Test
    @DisplayName("Guncelleme: kademe geriye alinamaz (TEST -> ANALIZ) 400 BusinessRule")
    void updateRejectsStageBackward() {
        when(weeklyReportRepository.findById(5L)).thenReturn(Optional.of(existingReport(5L, 30, ProgressStage.TEST)));

        assertThatThrownBy(() -> service.update(5L, request(30, ProgressStage.ANALIZ)))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("geriye");

        verify(weeklyReportRepository, never()).save(any());
    }

    @Test
    @DisplayName("Guncelleme: tam bir sonraki kademeye gecis kabul edilir (ANALIZ -> GELISTIRME)")
    void updateAllowsSingleStepAdvance() {
        when(weeklyReportRepository.findById(5L)).thenReturn(Optional.of(existingReport(5L, 30, ProgressStage.ANALIZ)));
        when(weeklyReportRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        WeeklyReportResponse response = service.update(5L, request(30, ProgressStage.GELISTIRME));

        assertThat(response.progressStage()).isEqualTo(ProgressStage.GELISTIRME);
        assertThat(response.progressPercentage()).isEqualTo(50);
        verify(weeklyReportRepository).save(any());
    }

    @Test
    @DisplayName("Guncelleme: hafta degisip cakisirsa 409 DuplicateResource")
    void updateRejectsWeekCollision() {
        when(weeklyReportRepository.findById(5L)).thenReturn(Optional.of(existingReport(5L, 30, ProgressStage.ANALIZ)));
        when(weeklyReportRepository.existsByProjectIdAndWeekNumber(1L, 31)).thenReturn(true);

        assertThatThrownBy(() -> service.update(5L, request(31, ProgressStage.ANALIZ)))
                .isInstanceOf(DuplicateResourceException.class);

        verify(weeklyReportRepository, never()).save(any());
    }

    @Test
    @DisplayName("Detay: olmayan rapor 404 ResourceNotFound")
    void findByIdRejectsMissingReport() {
        when(weeklyReportRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findById(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("Listeleme: olmayan proje icin 404 ResourceNotFound")
    void findByProjectRejectsMissingProject() {
        when(projectRepository.existsById(eq(99L))).thenReturn(false);

        assertThatThrownBy(() -> service.findByProject(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
