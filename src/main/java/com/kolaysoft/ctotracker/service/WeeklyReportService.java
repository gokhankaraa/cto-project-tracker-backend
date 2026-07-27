package com.kolaysoft.ctotracker.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kolaysoft.ctotracker.common.exception.BusinessRuleException;
import com.kolaysoft.ctotracker.common.exception.DuplicateResourceException;
import com.kolaysoft.ctotracker.common.exception.ResourceNotFoundException;
import com.kolaysoft.ctotracker.dto.WeeklyReportRequest;
import com.kolaysoft.ctotracker.dto.WeeklyReportResponse;
import com.kolaysoft.ctotracker.entity.ProgressStage;
import com.kolaysoft.ctotracker.entity.Project;
import com.kolaysoft.ctotracker.entity.WeeklyReport;
import com.kolaysoft.ctotracker.repository.ProjectRepository;
import com.kolaysoft.ctotracker.repository.WeeklyReportRepository;

/**
 * Haftalik rapor CRUD is mantigi ve is kurallari (on analiz H-03/H-04/H-05).
 *
 * <p>Uygulanan kurallar:
 * <ul>
 *   <li>Bir proje bir hafta icin yalnizca bir rapor alabilir (409 Conflict).</li>
 *   <li>Guncellemede ilerleme kademesi ayni kalabilir ya da tam bir sonraki kademeye
 *       gecebilir; geriye alinamaz ve sira atlanamaz (400 Bad Request). Olusturmada
 *       herhangi bir kademe kabul edilir (rapor, projenin o anki durumunu kaydeder).</li>
 *   <li>Ilerleme yuzdesi kademeden turer; ayrica saklanmaz.</li>
 *   <li>Bulunamayan proje/rapor 404 doner.</li>
 * </ul>
 */
@Service
@Transactional
public class WeeklyReportService {

    private final WeeklyReportRepository weeklyReportRepository;
    private final ProjectRepository projectRepository;

    public WeeklyReportService(WeeklyReportRepository weeklyReportRepository,
                               ProjectRepository projectRepository) {
        this.weeklyReportRepository = weeklyReportRepository;
        this.projectRepository = projectRepository;
    }

    @Transactional(readOnly = true)
    public List<WeeklyReportResponse> findByProject(Long projectId) {
        requireProjectExists(projectId);
        return weeklyReportRepository.findByProjectId(projectId).stream()
                .map(WeeklyReportService::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public WeeklyReportResponse findById(Long reportId) {
        return toResponse(getReportOrThrow(reportId));
    }

    public WeeklyReportResponse create(Long projectId, WeeklyReportRequest request) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Proje", projectId));

        if (weeklyReportRepository.existsByProjectIdAndWeekNumber(projectId, request.weekNumber())) {
            throw new DuplicateResourceException(
                    "Bu proje ve hafta icin rapor zaten mevcut (hafta %d).".formatted(request.weekNumber()));
        }

        WeeklyReport report = new WeeklyReport();
        report.setProject(project);
        applyRequest(report, request);
        return toResponse(weeklyReportRepository.save(report));
    }

    public WeeklyReportResponse update(Long reportId, WeeklyReportRequest request) {
        WeeklyReport report = getReportOrThrow(reportId);

        // Hafta numarasi degistiyse, yeni hafta ayni projede baska bir raporla cakismasin.
        boolean weekChanged = !report.getWeekNumber().equals(request.weekNumber());
        if (weekChanged && weeklyReportRepository.existsByProjectIdAndWeekNumber(
                report.getProject().getId(), request.weekNumber())) {
            throw new DuplicateResourceException(
                    "Bu proje ve hafta icin rapor zaten mevcut (hafta %d).".formatted(request.weekNumber()));
        }

        validateStageTransition(report.getProgressStage(), request.progressStage());
        applyRequest(report, request);
        return toResponse(weeklyReportRepository.save(report));
    }

    public void delete(Long reportId) {
        if (!weeklyReportRepository.existsById(reportId)) {
            throw new ResourceNotFoundException("Haftalik rapor", reportId);
        }
        weeklyReportRepository.deleteById(reportId);
    }

    /** Kademe ayni kalabilir ya da tam bir adim ilerleyebilir; geriye/atlayarak gecis reddedilir. */
    private void validateStageTransition(ProgressStage current, ProgressStage next) {
        int diff = next.ordinal() - current.ordinal();
        if (diff < 0) {
            throw new BusinessRuleException(
                    "Ilerleme kademesi geriye alinamaz: %s -> %s.".formatted(current, next));
        }
        if (diff > 1) {
            throw new BusinessRuleException(
                    "Ilerleme kademesinde sira atlanamaz; yalnizca bir sonraki kademeye gecilebilir: %s -> %s."
                            .formatted(current, next));
        }
    }

    private void applyRequest(WeeklyReport report, WeeklyReportRequest request) {
        report.setWeekNumber(request.weekNumber());
        report.setReportDate(request.reportDate());
        report.setProgressStage(request.progressStage());
        report.setOverallStatus(request.overallStatus());
        report.setRiskLevel(request.riskLevel());
        report.setDone(request.done());
        report.setPlanned(request.planned());
        report.setRisks(request.risks());
        report.setNote(request.note());
    }

    private WeeklyReport getReportOrThrow(Long reportId) {
        return weeklyReportRepository.findById(reportId)
                .orElseThrow(() -> new ResourceNotFoundException("Haftalik rapor", reportId));
    }

    private void requireProjectExists(Long projectId) {
        if (!projectRepository.existsById(projectId)) {
            throw new ResourceNotFoundException("Proje", projectId);
        }
    }

    private static WeeklyReportResponse toResponse(WeeklyReport r) {
        return new WeeklyReportResponse(
                r.getId(),
                r.getProject().getId(),
                r.getWeekNumber(),
                r.getReportDate(),
                r.getProgressStage(),
                r.getProgressStage().getPercentage(),
                r.getOverallStatus(),
                r.getRiskLevel(),
                r.getDone(),
                r.getPlanned(),
                r.getRisks(),
                r.getNote());
    }
}
