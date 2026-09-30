package com.gruponorte.mesaayudati.entity;

/** Estados permitidos para el ciclo de vida de un ticket. */
public enum TicketStatus {
    NUEVO,
    ASIGNADO,
    EN_ATENCION,
    PENDIENTE_USUARIO,
    RESUELTO,
    CERRADO,
    REABIERTO
}