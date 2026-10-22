package com.kolaysoft.ctotracker.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.kolaysoft.ctotracker.entity.WeeklyReport;

/** Filtreli sorgular icin bkz. {@code repository.spec.WeeklyReportSpecifications} (T13). */
public interface WeeklyReportRepository extends JpaRepository<WeeklyReport, Long>,
        JpaSpecificationExecutor<WeeklyReport> {

    /** Bir projenin tum haftalik raporlari. */
    List<WeeklyReport> findByProjectId(Long projectId);

    /** Projenin hic raporu var mi (proje silme guard'i icin). */
    boolean existsByProjectId(Long projectId);

    /** Projenin en guncel raporu: en yuksek hafta numarasina sahip olan (dashboard icin). */
    Optional<WeeklyReport> findTopByProjectIdOrderByWeekNumberDesc(Long projectId);

    /** Projenin verilen haftaya kadarki (dahil) en son raporu: dashboard'da referans hafta icin (T13). */
    Optional<WeeklyReport> findTopByProjectIdAndWeekNumberLessThanEqualOrderByWeekNumberDesc(
            Long projectId, Integer weekNumber);

    /** Proje + hafta benzersizlik kontrolu icin (on analiz H-03). */
    boolean existsByProjectIdAndWeekNumber(Long projectId, Integer weekNumber);
}
