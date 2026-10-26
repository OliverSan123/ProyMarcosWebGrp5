package com.gruponorte.mesaayudati.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/** Identificador del técnico al que el coordinador asigna el ticket. */
public record AssignTechnicianRequest(@NotNull(message = "El identificador es obligatorio") @Positive(message = "El identificador debe ser mayor que cero") Long technicianId) {
}