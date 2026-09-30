package com.gruponorte.mesaayudati.repository;

import com.gruponorte.mesaayudati.entity.TicketHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/** Solo se usa para insertar y consultar eventos; la API no expone edición ni borrado. */
public interface TicketHistoryRepository extends JpaRepository<TicketHistory, Long> {
    List<TicketHistory> findByTicketIdOrderByCreatedAtAsc(Long ticketId);
}