package com.gruponorte.mesaayudati.controller;

import com.gruponorte.mesaayudati.entity.Ticket;
import com.gruponorte.mesaayudati.service.TicketService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** API JSON de tickets consumible desde el frontend JavaScript. */
@RestController
@RequestMapping("/tickets")
public class TicketController {

    private final TicketService ticketService;

    public TicketController(TicketService ticketService) {
        this.ticketService = ticketService;
    }

    /** Lista tickets filtrando por prioridad, estado y área si se envían. */
    @GetMapping
    public ResponseEntity<List<Ticket>> listarTickets(
            @RequestParam(required = false) String prioridad,
            @RequestParam(required = false) String estado,
            @RequestParam(required = false) String area) {
        return ResponseEntity.ok(ticketService.filtrarTickets(prioridad, estado, area));
    }

    /** Recalcula el SLA y devuelve el ticket actualizado en formato JSON. */
    @PutMapping("/{id}/sla")
    public ResponseEntity<Ticket> recalcularSla(@PathVariable Long id) {
        return ResponseEntity.ok(ticketService.calcularFechaObjetivo(id));
    }
}