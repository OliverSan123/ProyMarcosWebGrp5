package com.gruponorte.mesaayudati.dto;

import com.gruponorte.mesaayudati.entity.ImpactLevel;
import jakarta.validation.constraints.NotNull;

/** Regla de matriz seleccionada por el administrador. */
public record PriorityRuleRequest(@NotNull ImpactLevel impact,
                                  @NotNull ImpactLevel urgency,
                                  @NotNull Long priorityId) {
}