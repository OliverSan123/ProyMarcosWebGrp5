package com.gruponorte.mesaayudati.service;

import com.gruponorte.mesaayudati.dto.AssignTechnicianRequest;
import com.gruponorte.mesaayudati.dto.CommentRequest;
import com.gruponorte.mesaayudati.dto.ReopenTicketRequest;
import com.gruponorte.mesaayudati.dto.TicketCreateRequest;
import com.gruponorte.mesaayudati.dto.TicketHistoryResponse;
import com.gruponorte.mesaayudati.dto.TicketResponse;
import com.gruponorte.mesaayudati.dto.TicketStatusRequest;
import com.gruponorte.mesaayudati.entity.TicketStatus;

import java.util.List;

/** Define las operaciones de negocio, autorización y trazabilidad de tickets. */
public interface TicketService {
    TicketResponse crear(TicketCreateRequest request, String username);
    List<TicketResponse> filtrar(Long technicianId, Long areaId, Long priorityId,
                                 Long categoryId, TicketStatus state, String username);
    TicketResponse obtener(Long id, String username);
    TicketResponse asignar(Long id, AssignTechnicianRequest request, String username);
    TicketResponse cambiarEstado(Long id, TicketStatusRequest request, String username);
    TicketHistoryResponse comentar(Long id, CommentRequest request, String username);
    TicketResponse reabrir(Long id, ReopenTicketRequest request, String username);
    TicketResponse cerrar(Long id, String username);
    List<TicketHistoryResponse> historial(Long id, String username);
    TicketResponse calcularFechaObjetivo(Long id, String username);
}