package com.gruponorte.mesaayudati.dto;

/** Regla de prioridad presentada en forma plana al frontend. */
public record PriorityRuleResponse(Long id, String impact, String urgency,
                                   Long priorityId, String priority) {
}