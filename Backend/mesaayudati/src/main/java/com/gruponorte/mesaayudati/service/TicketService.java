package com.gruponorte.mesaayudati.service;

import com.gruponorte.mesaayudati.entity.Ticket;

import java.util.List;

/** Define las operaciones de negocio de consulta y SLA de tickets. */
public interface TicketService {

    /** Busca tickets usando los filtros que se hayan indicado. */
    List<Ticket> filtrarTickets(String prioridad, String estado, String area);

    /** Calcula y guarda la fecha objetivo según la prioridad del ticket. */
    Ticket calcularFechaObjetivo(Long id);
}