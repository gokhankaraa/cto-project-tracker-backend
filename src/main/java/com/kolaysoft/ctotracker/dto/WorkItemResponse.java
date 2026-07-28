package com.kolaysoft.ctotracker.dto;

import java.time.LocalDate;

import com.kolaysoft.ctotracker.entity.WorkItemStatus;

import io.swagger.v3.oas.annotations.media.Schema;

/** İş kalemi yanıtı. */
@Schema(description = "İş kalemi yanıtı")
public record WorkItemResponse(

        @Schema(description = "İş kalemi id'si", example = "1")
        Long id,

        @Schema(description = "Bağlı olduğu haftalık rapor id'si", example = "1")
        Long weeklyReportId,

        @Schema(description = "Başlık", example = "Rapor formu API'si")
        String title,

        @Schema(description = "Açıklama")
        String description,

        @Schema(description = "Sorumlu", example = "Ayşe Yılmaz")
        String assignee,

        @Schema(description = "Durum", example = "DEVAM_EDIYOR")
        WorkItemStatus status,

        @Schema(description = "Planlanan tarih", example = "2026-07-28")
        LocalDate plannedDate,

        @Schema(description = "Tamamlanan tarih", example = "2026-07-31")
        LocalDate completedDate) {
}
