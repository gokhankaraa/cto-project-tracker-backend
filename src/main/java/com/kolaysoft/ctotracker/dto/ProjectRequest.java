package com.kolaysoft.ctotracker.dto;

import com.kolaysoft.ctotracker.entity.ProjectStatus;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/** Proje olusturma/guncelleme istegi. */
@Schema(description = "Proje olusturma/guncelleme istegi")
public record ProjectRequest(

        @Schema(description = "Proje adi", example = "PEYK")
        @NotBlank(message = "Proje adi zorunludur.")
        String name,

        @Schema(description = "Musteri veya demo musteri", example = "Kolaysoft")
        String customer,

        @Schema(description = "Proje aciklamasi", example = "Insan kaynaklari surecleri urunu")
        String description,

        @Schema(description = "Proje durumu", example = "DEVAM_EDIYOR")
        @NotNull(message = "Proje durumu zorunludur.")
        ProjectStatus status,

        @Schema(description = "Sorumlu proje yoneticisinin kullanici id'si", example = "1")
        @NotNull(message = "Sorumlu proje yoneticisi (ownerId) zorunludur.")
        Long ownerId) {
}
