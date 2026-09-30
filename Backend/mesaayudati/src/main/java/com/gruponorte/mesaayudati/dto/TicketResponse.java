package com.gruponorte.mesaayudati.dto;

import java.time.LocalDateTime;

/** Datos seguros y planos del ticket para respuestas JSON. */
public record TicketResponse(
        Long id, String title, String description,
        Long categoryId, String category,
        Long areaId, String area,
        Long priorityId, String priority, int slaHours,
        String impact, String urgency, String state,
        Long creatorId, String creator,
        Long technicianId, String technician,
        LocalDateTime createdAt, LocalDateTime targetAt, LocalDateTime closedAt) {
}