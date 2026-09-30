package com.gruponorte.mesaayudati.service.impl;

import com.gruponorte.mesaayudati.dto.AssignTechnicianRequest;
import com.gruponorte.mesaayudati.dto.CommentRequest;
import com.gruponorte.mesaayudati.dto.ReopenTicketRequest;
import com.gruponorte.mesaayudati.dto.TicketCreateRequest;
import com.gruponorte.mesaayudati.dto.TicketHistoryResponse;
import com.gruponorte.mesaayudati.dto.TicketResponse;
import com.gruponorte.mesaayudati.dto.TicketStatusRequest;
import com.gruponorte.mesaayudati.entity.AppUser;
import com.gruponorte.mesaayudati.entity.Category;
import com.gruponorte.mesaayudati.entity.Priority;
import com.gruponorte.mesaayudati.entity.ImpactLevel;
import com.gruponorte.mesaayudati.entity.PriorityRule;
import com.gruponorte.mesaayudati.entity.Ticket;
import com.gruponorte.mesaayudati.entity.TicketHistory;
import com.gruponorte.mesaayudati.entity.TicketStatus;
import com.gruponorte.mesaayudati.entity.UserRole;
import com.gruponorte.mesaayudati.repository.AreaRepository;
import com.gruponorte.mesaayudati.repository.CategoryRepository;
import com.gruponorte.mesaayudati.repository.PriorityRuleRepository;
import com.gruponorte.mesaayudati.repository.TicketHistoryRepository;
import com.gruponorte.mesaayudati.repository.TicketRepository;
import com.gruponorte.mesaayudati.repository.UserRepository;
import com.gruponorte.mesaayudati.service.TicketService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

/** Implementa las reglas de negocio y autorización de cada operación de tickets. */
@Service
@Transactional
public class TicketServiceImpl implements TicketService {

    private final TicketRepository ticketRepository;
    private final TicketHistoryRepository historyRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final AreaRepository areaRepository;
    private final PriorityRuleRepository priorityRuleRepository;

    public TicketServiceImpl(TicketRepository ticketRepository, TicketHistoryRepository historyRepository,
                             UserRepository userRepository, CategoryRepository categoryRepository,
                             AreaRepository areaRepository, PriorityRuleRepository priorityRuleRepository) {
        this.ticketRepository = ticketRepository;
        this.historyRepository = historyRepository;
        this.userRepository = userRepository;
        this.categoryRepository = categoryRepository;
        this.areaRepository = areaRepository;
        this.priorityRuleRepository = priorityRuleRepository;
    }

    /** Registra el ticket, calcula prioridad/SLA en servidor y deja el evento inicial. */
    @Override
    public TicketResponse crear(TicketCreateRequest request, String username) {
        AppUser requester = currentUser(username);
        if (requester.getRole() != UserRole.SOLICITANTE) {
            throw forbidden("Solo un solicitante puede registrar tickets");
        }

        Category category = categoryRepository.findById(request.categoryId())
                .orElseThrow(() -> notFound("Categoría"));
        var area = areaRepository.findById(request.areaId()).orElseThrow(() -> notFound("Área"));
        Priority priority = calculatePriority(request.impact(), request.urgency());
        LocalDateTime now = LocalDateTime.now();

        Ticket ticket = new Ticket();
        ticket.setTitle(request.title().trim());
        ticket.setDescription(request.description().trim());
        ticket.setCategory(category);
        ticket.setArea(area);
        ticket.setPriority(priority);
        ticket.setCreator(requester);
        ticket.setImpact(request.impact());
        ticket.setUrgency(request.urgency());
        ticket.setState(TicketStatus.NUEVO);
        ticket.setCreatedAt(now);
        ticket.setTargetAt(now.plusHours(priority.getSlaHours()));
        ticket = ticketRepository.save(ticket);
        addHistory(ticket, "CREACION", null, TicketStatus.NUEVO.name(), null, requester);
        return toResponse(ticket);
    }

    /** Aplica todos los filtros opcionales y limita el resultado del solicitante a lo propio. */
    @Override
    @Transactional(readOnly = true)
    public List<TicketResponse> filtrar(Long technicianId, Long areaId, Long priorityId,
                                        Long categoryId, TicketStatus state, String username) {
        AppUser requester = currentUser(username);
        Long ownerId = requester.getRole() == UserRole.SOLICITANTE ? requester.getId() : null;
        return ticketRepository.buscarConFiltros(technicianId, areaId, priorityId, categoryId, state, ownerId)
                .stream().map(this::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public TicketResponse obtener(Long id, String username) {
        AppUser user = currentUser(username);
        Ticket ticket = getTicket(id);
        ensureVisible(ticket, user);
        return toResponse(ticket);
    }

    /** El coordinador asigna o reasigna a una cuenta técnica habilitada y registra los cambios. */
    @Override
    public TicketResponse asignar(Long id, AssignTechnicianRequest request, String username) {
        AppUser coordinator = currentUser(username);
        Ticket ticket = getTicket(id);
        ensureNotClosed(ticket);
        if (ticket.getState() == TicketStatus.RESUELTO) {
            throw conflict("No se puede reasignar un ticket resuelto");
        }

        AppUser newTechnician = userRepository.findById(request.technicianId())
                .orElseThrow(() -> notFound("Técnico"));
        if (newTechnician.getRole() != UserRole.TECNICO || !newTechnician.isEnabled()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "La cuenta seleccionada no es un técnico habilitado");
        }

        AppUser previousTechnician = ticket.getAssignedTechnician();
        String oldName = previousTechnician == null ? null : previousTechnician.getUsername();
        ticket.setAssignedTechnician(newTechnician);
        TicketStatus previousState = ticket.getState();
        ticket.setState(TicketStatus.ASIGNADO);
        ticket = ticketRepository.save(ticket);
        addHistory(ticket, "ASIGNACION", oldName, newTechnician.getUsername(), null, coordinator);
        if (previousState != TicketStatus.ASIGNADO) {
            addHistory(ticket, "ESTADO", previousState.name(), TicketStatus.ASIGNADO.name(), null, coordinator);
        }
        return toResponse(ticket);
    }

    /** Solo el técnico asignado puede avanzar los estados permitidos de atención. */
    @Override
    public TicketResponse cambiarEstado(Long id, TicketStatusRequest request, String username) {
        AppUser technician = currentUser(username);
        Ticket ticket = getTicket(id);
        ensureAssignedTechnician(ticket, technician);
        ensureNotClosed(ticket);
        TicketStatus next = request.state();
        if (!isValidTechnicianTransition(ticket.getState(), next)) {
            throw conflict("La transición de estado solicitada no está permitida");
        }

        TicketStatus previous = ticket.getState();
        ticket.setState(next);
        ticket = ticketRepository.save(ticket);
        addHistory(ticket, "ESTADO", previous.name(), next.name(), null, technician);
        return toResponse(ticket);
    }

    /** Guarda comentarios como eventos del historial, con autor y fecha. */
    @Override
    public TicketHistoryResponse comentar(Long id, CommentRequest request, String username) {
        AppUser author = currentUser(username);
        Ticket ticket = getTicket(id);
        ensureVisible(ticket, author);
        return toHistoryResponse(addHistory(ticket, "COMENTARIO", null, null,
                request.comment().trim(), author));
    }

    /** Reabre solo un ticket resuelto/cerrado y conserva el motivo como auditoría. */
    @Override
    public TicketResponse reabrir(Long id, ReopenTicketRequest request, String username) {
        AppUser requester = currentUser(username);
        Ticket ticket = getTicket(id);
        ensureVisible(ticket, requester);
        if (ticket.getState() != TicketStatus.RESUELTO && ticket.getState() != TicketStatus.CERRADO) {
            throw conflict("Solo se puede reabrir un ticket resuelto o cerrado");
        }

        TicketStatus previous = ticket.getState();
        ticket.setState(TicketStatus.REABIERTO);
        ticket.setClosedAt(null);
        ticket = ticketRepository.save(ticket);
        addHistory(ticket, "REAPERTURA", previous.name(), TicketStatus.REABIERTO.name(),
                request.reason().trim(), requester);
        return toResponse(ticket);
    }

    /** Impide cerrar un ticket que todavía no está resuelto. */
    @Override
    public TicketResponse cerrar(Long id, String username) {
        AppUser actor = currentUser(username);
        Ticket ticket = getTicket(id);
        if (actor.getRole() == UserRole.TECNICO) ensureAssignedTechnician(ticket, actor);
        if (ticket.getState() != TicketStatus.RESUELTO) {
            throw conflict("Solo se puede cerrar un ticket resuelto");
        }

        LocalDateTime now = LocalDateTime.now();
        ticket.setState(TicketStatus.CERRADO);
        ticket.setClosedAt(now);
        ticket = ticketRepository.save(ticket);
        addHistory(ticket, "CIERRE", TicketStatus.RESUELTO.name(), TicketStatus.CERRADO.name(), null, actor);
        return toResponse(ticket);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TicketHistoryResponse> historial(Long id, String username) {
        AppUser user = currentUser(username);
        ensureVisible(getTicket(id), user);
        return historyRepository.findByTicketIdOrderByCreatedAtAsc(id).stream()
                .map(this::toHistoryResponse).toList();
    }

    /** Conserva el endpoint inicial de RF-10 y usa las horas configurables del catálogo. */
    @Override
    public TicketResponse calcularFechaObjetivo(Long id, String username) {
        AppUser user = currentUser(username);
        Ticket ticket = getTicket(id);
        ensureVisible(ticket, user);
        ticket.setTargetAt(ticket.getCreatedAt().plusHours(ticket.getPriority().getSlaHours()));
        return toResponse(ticketRepository.save(ticket));
    }

    /** Busca la regla configurada por el administrador para impacto y urgencia. */
    private Priority calculatePriority(ImpactLevel impact, ImpactLevel urgency) {
        return priorityRuleRepository.findByImpactAndUrgency(impact, urgency)
                .map(PriorityRule::getPriority)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.CONFLICT,
                        "No existe una regla configurada para ese impacto y urgencia"));
    }

    /** Valida el flujo permitido, sin saltos arbitrarios entre estados. */
    private boolean isValidTechnicianTransition(TicketStatus current, TicketStatus next) {
        return switch (current) {
            case ASIGNADO -> next == TicketStatus.EN_ATENCION;
            case EN_ATENCION -> next == TicketStatus.PENDIENTE_USUARIO || next == TicketStatus.RESUELTO;
            case PENDIENTE_USUARIO -> next == TicketStatus.EN_ATENCION || next == TicketStatus.RESUELTO;
            case REABIERTO -> next == TicketStatus.EN_ATENCION;
            default -> false;
        };
    }

    private AppUser currentUser(String username) {
        return userRepository.findByUsernameIgnoreCase(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuario no encontrado"));
    }

    private Ticket getTicket(Long id) {
        return ticketRepository.findById(id).orElseThrow(() -> notFound("Ticket"));
    }

    private void ensureVisible(Ticket ticket, AppUser user) {
        if (user.getRole() == UserRole.SOLICITANTE
                && !ticket.getCreator().getId().equals(user.getId())) {
            throw forbidden("Solo puedes consultar tus propios tickets");
        }
    }

    private void ensureAssignedTechnician(Ticket ticket, AppUser user) {
        if (user.getRole() != UserRole.TECNICO || ticket.getAssignedTechnician() == null
                || !ticket.getAssignedTechnician().getId().equals(user.getId())) {
            throw forbidden("Solo el técnico asignado puede modificar este ticket");
        }
    }

    private void ensureNotClosed(Ticket ticket) {
        if (ticket.getState() == TicketStatus.CERRADO) throw conflict("El ticket cerrado no puede modificarse");
    }

    private TicketHistory addHistory(Ticket ticket, String event, String oldValue, String newValue,
                                     String details, AppUser author) {
        return historyRepository.save(new TicketHistory(ticket, event, oldValue, newValue,
                details, author, LocalDateTime.now()));
    }

    private TicketResponse toResponse(Ticket ticket) {
        AppUser technician = ticket.getAssignedTechnician();
        return new TicketResponse(ticket.getId(), ticket.getTitle(), ticket.getDescription(),
                ticket.getCategory().getId(), ticket.getCategory().getName(),
                ticket.getArea().getId(), ticket.getArea().getName(),
                ticket.getPriority().getId(), ticket.getPriority().getName(), ticket.getPriority().getSlaHours(),
                ticket.getImpact().name(), ticket.getUrgency().name(), ticket.getState().name(),
                ticket.getCreator().getId(), ticket.getCreator().getUsername(),
                technician == null ? null : technician.getId(),
                technician == null ? null : technician.getUsername(),
                ticket.getCreatedAt(), ticket.getTargetAt(), ticket.getClosedAt());
    }

    private TicketHistoryResponse toHistoryResponse(TicketHistory history) {
        return new TicketHistoryResponse(history.getId(), history.getEventType(), history.getOldValue(),
                history.getNewValue(), history.getDetails(), history.getAuthor().getUsername(),
                history.getCreatedAt());
    }

    private ResponseStatusException notFound(String type) {
        return new ResponseStatusException(HttpStatus.NOT_FOUND, type + " no encontrado");
    }

    private ResponseStatusException forbidden(String message) {
        return new ResponseStatusException(HttpStatus.FORBIDDEN, message);
    }

    private ResponseStatusException conflict(String message) {
        return new ResponseStatusException(HttpStatus.CONFLICT, message);
    }
}