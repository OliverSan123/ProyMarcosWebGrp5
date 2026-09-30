package com.gruponorte.mesaayudati.repository;

import com.gruponorte.mesaayudati.entity.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/** Consultas de persistencia para tickets y filtros opcionales. */
public interface TicketRepository extends JpaRepository<Ticket, Long> {

    /** Omite un filtro cuando su valor no se especifica o llega vacío. */
    @Query("""
            SELECT ticket FROM Ticket ticket
            WHERE (:prioridad IS NULL OR :prioridad = '' OR LOWER(ticket.prioridad) = LOWER(:prioridad))
              AND (:estado IS NULL OR :estado = '' OR LOWER(ticket.estado) = LOWER(:estado))
              AND (:area IS NULL OR :area = '' OR LOWER(ticket.area) = LOWER(:area))
            """)
    List<Ticket> buscarConFiltros(
            @Param("prioridad") String prioridad,
            @Param("estado") String estado,
            @Param("area") String area);
}