package com.gruponorte.mesaayudati.controller;

import com.gruponorte.mesaayudati.dto.AssignTechnicianRequest;
import com.gruponorte.mesaayudati.dto.CommentRequest;
import com.gruponorte.mesaayudati.dto.ReopenTicketRequest;
import com.gruponorte.mesaayudati.dto.TicketCreateRequest;
import com.gruponorte.mesaayudati.dto.TicketHistoryResponse;
import com.gruponorte.mesaayudati.dto.TicketResponse;
import com.gruponorte.mesaayudati.dto.TicketStatusRequest;
import com.gruponorte.mesaayudati.entity.TicketStatus;
import com.gruponorte.mesaayudati.service.TicketService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** API JSON para registro, consulta y flujo de atención de tickets. */
@RestController
@RequestMapping("/api/tickets")
public class TicketController {

    private final TicketService ticketService;

    public TicketController(TicketService ticketService) {
        this.ticketService = ticketService;
    }

    /** RF-04: solo el solicitante registra tickets nuevos. */
    @PostMapping
    @PreAuthorize("hasRole('SOLICITANTE')")
    public ResponseEntity<TicketResponse> crear(@Valid @RequestBody TicketCreateRequest request,
                                                Authentication authentication) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ticketService.crear(request, authentication.getName()));
    }

    /** RF-11/RF-12: aplica filtros opcionales y respeta la visibilidad del solicitante. */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMINISTRADOR','COORDINADOR','TECNICO','SOLICITANTE')")
    public List<TicketResponse> listarTickets(
            @RequestParam(required = false) Long tecnicoId,
            @RequestParam(required = false) Long areaId,
            @RequestParam(required = false) Long prioridadId,
            @RequestParam(required = false) Long categoriaId,
            @RequestParam(required = false) TicketStatus estado,
            Authentication authentication) {
        return ticketService.filtrar(tecnicoId, areaId, prioridadId, categoriaId,
                estado, authentication.getName());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR','COORDINADOR','TECNICO','SOLICITANTE')")
    public TicketResponse obtener(@PathVariable Long id, Authentication authentication) {
        return ticketService.obtener(id, authentication.getName());
    }

    /** RF-06: el coordinador asigna o reasigna al técnico indicado. */
    @PutMapping("/{id}/asignacion")
    @PreAuthorize("hasRole('COORDINADOR')")
    public TicketResponse asignar(@PathVariable Long id,
                                  @Valid @RequestBody AssignTechnicianRequest request,
                                  Authentication authentication) {
        return ticketService.asignar(id, request, authentication.getName());
    }

    /** RF-07: solo el técnico asignado puede cambiar los estados permitidos. */
    @PutMapping("/{id}/estado")
    @PreAuthorize("hasRole('TECNICO')")
    public TicketResponse cambiarEstado(@PathVariable Long id,
                                        @Valid @RequestBody TicketStatusRequest request,
                                        Authentication authentication) {
        return ticketService.cambiarEstado(id, request, authentication.getName());
    }

    /** RF-08: añade comentarios cronológicos con autor y fecha. */
    @PostMapping("/{id}/comentarios")
    @PreAuthorize("hasAnyRole('TECNICO','COORDINADOR','SOLICITANTE')")
    public ResponseEntity<TicketHistoryResponse> comentar(@PathVariable Long id,
                                                          @Valid @RequestBody CommentRequest request,
                                                          Authentication authentication) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ticketService.comentar(id, request, authentication.getName()));
    }

    /** RF-09: el motivo de reapertura es obligatorio. */
    @PutMapping("/{id}/reapertura")
    @PreAuthorize("hasRole('SOLICITANTE')")
    public TicketResponse reabrir(@PathVariable Long id,
                                  @Valid @RequestBody ReopenTicketRequest request,
                                  Authentication authentication) {
        return ticketService.reabrir(id, request, authentication.getName());
    }

    /** RNF-05: solo se puede cerrar un ticket resuelto. */
    @PutMapping("/{id}/cierre")
    @PreAuthorize("hasAnyRole('COORDINADOR','TECNICO')")
    public TicketResponse cerrar(@PathVariable Long id, Authentication authentication) {
        return ticketService.cerrar(id, authentication.getName());
    }

    @GetMapping("/{id}/historial")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR','COORDINADOR','TECNICO','SOLICITANTE')")
    public List<TicketHistoryResponse> historial(@PathVariable Long id, Authentication authentication) {
        return ticketService.historial(id, authentication.getName());
    }

    /** RF-10: recalcula usando las horas del catálogo de prioridad. */
    @PutMapping("/{id}/sla")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR','COORDINADOR','TECNICO','SOLICITANTE')")
    public TicketResponse recalcularSla(@PathVariable Long id, Authentication authentication) {
        return ticketService.calcularFechaObjetivo(id, authentication.getName());
    }
}
