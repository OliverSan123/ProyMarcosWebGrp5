package com.gruponorte.mesaayudati.dto;

import com.gruponorte.mesaayudati.entity.ImpactLevel;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/** Regla de matriz seleccionada por el administrador. */
public record PriorityRuleRequest(@NotNull ImpactLevel impact,
                                  @NotNull ImpactLevel urgency,
                                  @NotNull(message = "El identificador es obligatorio") @Positive(message = "El identificador debe ser mayor que cero") Long priorityId) {
}