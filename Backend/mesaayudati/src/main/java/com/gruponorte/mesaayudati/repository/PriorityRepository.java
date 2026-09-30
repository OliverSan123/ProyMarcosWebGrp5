package com.gruponorte.mesaayudati.repository;

import com.gruponorte.mesaayudati.entity.Priority;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/** Persistencia del catálogo de prioridades y sus SLA. */
public interface PriorityRepository extends JpaRepository<Priority, Long> {
    Optional<Priority> findByNameIgnoreCase(String name);
    boolean existsByNameIgnoreCase(String name);
}