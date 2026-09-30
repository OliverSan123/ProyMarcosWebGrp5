package com.gruponorte.mesaayudati.service.impl;

import com.gruponorte.mesaayudati.entity.Ticket;
import com.gruponorte.mesaayudati.repository.TicketRepository;
import com.gruponorte.mesaayudati.service.TicketService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;

/** Implementa las reglas de consulta y cálculo del SLA. */
@Service
public class TicketServiceImpl implements TicketService {

    private final TicketRepository ticketRepository;

    public TicketServiceImpl(TicketRepository ticketRepository) {
        this.ticketRepository = ticketRepository;
    }

    /** Delega al repositorio la búsqueda con filtros opcionales. */
    @Override
    public List<Ticket> filtrarTickets(String prioridad, String estado, String area) {
        return ticketRepository.buscarConFiltros(prioridad, estado, area);
    }

    /** Calcula la fecha objetivo desde la creación y persiste el resultado. */
    @Override
    public Ticket calcularFechaObjetivo(Long id) {
        Ticket ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Ticket no encontrado"));

        if (ticket.getFechaCreacion() == null) {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY,
                    "El ticket no tiene fecha de creación");
        }

        int horasSla = obtenerHorasSla(ticket.getPrioridad());
        LocalDateTime fechaObjetivo = ticket.getFechaCreacion().plusHours(horasSla);
        ticket.setFechaObjetivo(fechaObjetivo);

        return ticketRepository.save(ticket);
    }

    /** Aplica la tabla de horas definida para cada prioridad. */
    private int obtenerHorasSla(String prioridad) {
        if (prioridad == null || prioridad.isBlank()) {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY,
                    "El ticket no tiene prioridad");
        }

        return switch (prioridad.trim().toUpperCase(Locale.ROOT)) {
            case "CRITICA" -> 4;
            case "ALTA" -> 8;
            case "MEDIA" -> 24;
            case "BAJA" -> 72;
            default -> throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY,
                    "Prioridad sin regla SLA: " + prioridad);
        };
    }
}