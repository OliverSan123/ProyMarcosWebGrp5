package com.gruponorte.mesaayudati.repository;

import com.gruponorte.mesaayudati.entity.Area;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/** Persistencia y búsqueda de áreas por nombre. */
public interface AreaRepository extends JpaRepository<Area, Long> {
    Optional<Area> findByNameIgnoreCase(String name);
    boolean existsByNameIgnoreCase(String name);
}