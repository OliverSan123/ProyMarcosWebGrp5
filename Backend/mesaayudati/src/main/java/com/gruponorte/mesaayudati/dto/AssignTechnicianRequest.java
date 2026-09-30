package com.gruponorte.mesaayudati.dto;

import jakarta.validation.constraints.NotNull;

/** Identificador del técnico al que el coordinador asigna el ticket. */
public record AssignTechnicianRequest(@NotNull Long technicianId) {
}