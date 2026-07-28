package com.kolaysoft.ctotracker.entity;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Bir projenin belirli bir haftaya ait durum raporu (on analiz, bolum 4).
 *
 * <p>Bir proje bir hafta icin yalnizca bir rapor alabilir: {@code (project_id, week_number)}
 * uzerinde benzersizlik kisiti tanimlidir. Bu kural veritabani duzeyinde garanti edilir;
 * servis katmani da ayni durumu onceden kontrol edip anlamli 409 hatasi dondurur.
 */
@Entity
@Table(name = "weekly_reports",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_weekly_report_project_week",
                columnNames = {"project_id", "week_number"}))
@Getter
@Setter
@NoArgsConstructor
public class WeeklyReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @Column(nullable = false)
    private LocalDate reportDate;

    @Column(name = "week_number", nullable = false)
    private Integer weekNumber;

    /** Ilerleme kademesi; yuzde bu kademeden turer (bkz. {@link ProgressStage}). */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProgressStage progressStage;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OverallStatus overallStatus;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RiskLevel riskLevel;

    /** O hafta yapilanlar (serbest metin). */
    @Column(length = 2000)
    private String done;

    /** Gelecek hafta yapilacaklar (serbest metin). */
    @Column(length = 2000)
    private String planned;

    /** Riskler / engeller (serbest metin). */
    @Column(length = 2000)
    private String risks;

    /** Genel durum notu (serbest metin). */
    @Column(length = 2000)
    private String note;

    /** Rapora bağlı iş kalemleri; rapor silinince bunlar da silinir. */
    @OneToMany(mappedBy = "weeklyReport", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<WorkItem> workItems = new ArrayList<>();
}
