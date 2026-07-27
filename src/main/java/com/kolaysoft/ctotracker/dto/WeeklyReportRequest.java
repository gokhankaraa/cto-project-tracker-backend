package com.kolaysoft.ctotracker.dto;

import java.time.LocalDate;

import com.kolaysoft.ctotracker.entity.OverallStatus;
import com.kolaysoft.ctotracker.entity.ProgressStage;
import com.kolaysoft.ctotracker.entity.RiskLevel;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/**
 * Haftalik rapor olusturma/guncelleme istegi.
 * Proje bilgisi URL yolundan gelir; ilerleme yuzdesi kademeden turedigi icin burada yer almaz.
 */
@Schema(description = "Haftalik rapor olusturma/guncelleme istegi")
public record WeeklyReportRequest(

        @Schema(description = "Hafta numarasi (1-53)", example = "30")
        @NotNull(message = "Hafta numarasi zorunludur.")
        @Min(value = 1, message = "Hafta numarasi en az 1 olmalidir.")
        @Max(value = 53, message = "Hafta numarasi en fazla 53 olmalidir.")
        Integer weekNumber,

        @Schema(description = "Rapor tarihi", example = "2026-07-27")
        @NotNull(message = "Rapor tarihi zorunludur.")
        LocalDate reportDate,

        @Schema(description = "Ilerleme kademesi", example = "GELISTIRME")
        @NotNull(message = "Ilerleme kademesi zorunludur.")
        ProgressStage progressStage,

        @Schema(description = "Genel durum", example = "YOLUNDA")
        @NotNull(message = "Genel durum zorunludur.")
        OverallStatus overallStatus,

        @Schema(description = "Risk seviyesi", example = "ORTA")
        @NotNull(message = "Risk seviyesi zorunludur.")
        RiskLevel riskLevel,

        @Schema(description = "O hafta yapilanlar")
        String done,

        @Schema(description = "Gelecek hafta yapilacaklar")
        String planned,

        @Schema(description = "Riskler / engeller")
        String risks,

        @Schema(description = "Genel durum notu")
        String note) {
}
