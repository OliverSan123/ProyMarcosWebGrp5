package com.gruponorte.mesaayudati.dto;

import com.gruponorte.mesaayudati.entity.TicketStatus;
import jakarta.validation.constraints.NotNull;

/** Estado solicitado por el técnico asignado. */
public record TicketStatusRequest(@NotNull TicketStatus state) {
}