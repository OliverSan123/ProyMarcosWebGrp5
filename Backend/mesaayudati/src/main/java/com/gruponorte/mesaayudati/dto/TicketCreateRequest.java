package com.gruponorte.mesaayudati.dto;

import com.gruponorte.mesaayudati.entity.ImpactLevel;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

/** Datos que el solicitante envía al registrar un ticket. */
public record TicketCreateRequest(
        @NotBlank(message = "El título es obligatorio") @Size(max = 150, message = "El título admite hasta 150 caracteres") String title,
        @NotBlank(message = "La descripción es obligatoria") @Size(max = 5000, message = "La descripción admite hasta 5000 caracteres") String description,
        @NotNull(message = "El identificador es obligatorio") @Positive(message = "El identificador debe ser mayor que cero") Long categoryId,
        @NotNull(message = "El identificador es obligatorio") @Positive(message = "El identificador debe ser mayor que cero") Long areaId,
        @NotNull ImpactLevel impact,
        @NotNull ImpactLevel urgency) {
}