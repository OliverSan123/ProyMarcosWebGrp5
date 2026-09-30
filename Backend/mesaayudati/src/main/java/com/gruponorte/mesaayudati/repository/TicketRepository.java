package com.gruponorte.mesaayudati.repository;

import com.gruponorte.mesaayudati.entity.Ticket;
import com.gruponorte.mesaayudati.entity.TicketStatus;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/** Consultas de persistencia con las relaciones necesarias para respuestas y filtros. */
public interface TicketRepository extends JpaRepository<Ticket, Long> {

    /** Carga relaciones en la consulta para evitar una consulta repetida por cada ticket. */
    @EntityGraph(attributePaths = {"creator", "assignedTechnician", "area", "category", "priority"})
    @Query("""
            SELECT ticket FROM Ticket ticket
            LEFT JOIN ticket.assignedTechnician assignedTechnician
            WHERE (:technicianId IS NULL OR assignedTechnician.id = :technicianId)
              AND (:areaId IS NULL OR ticket.area.id = :areaId)
              AND (:priorityId IS NULL OR ticket.priority.id = :priorityId)
              AND (:categoryId IS NULL OR ticket.category.id = :categoryId)
              AND (:state IS NULL OR ticket.state = :state)
              AND (:requesterId IS NULL OR ticket.creator.id = :requesterId)
            ORDER BY ticket.createdAt DESC
            """)
    List<Ticket> buscarConFiltros(
            @Param("technicianId") Long technicianId,
            @Param("areaId") Long areaId,
            @Param("priorityId") Long priorityId,
            @Param("categoryId") Long categoryId,
            @Param("state") TicketStatus state,
            @Param("requesterId") Long requesterId);
}