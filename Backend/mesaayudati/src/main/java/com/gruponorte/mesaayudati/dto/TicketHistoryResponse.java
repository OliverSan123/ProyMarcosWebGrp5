package com.gruponorte.mesaayudati.dto;

import java.time.LocalDateTime;

/** Evento inmutable del historial del ticket. */
public record TicketHistoryResponse(Long id, String eventType, String oldValue, String newValue,
                                    String details, String author, LocalDateTime createdAt) {
}