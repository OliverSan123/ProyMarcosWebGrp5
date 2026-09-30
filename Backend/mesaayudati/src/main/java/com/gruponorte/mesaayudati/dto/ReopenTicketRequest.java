package com.gruponorte.mesaayudati.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Motivo obligatorio que deja trazabilidad de la reapertura. */
public record ReopenTicketRequest(@NotBlank @Size(max = 1000) String reason) {
}