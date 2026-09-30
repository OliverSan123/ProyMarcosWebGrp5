package com.gruponorte.mesaayudati.dto;

import com.gruponorte.mesaayudati.entity.ImpactLevel;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** Datos que el solicitante envía al registrar un ticket. */
public record TicketCreateRequest(
        @NotBlank @Size(max = 150) String title,
        @NotBlank @Size(max = 5000) String description,
        @NotNull Long categoryId,
        @NotNull Long areaId,
        @NotNull ImpactLevel impact,
        @NotNull ImpactLevel urgency) {
}