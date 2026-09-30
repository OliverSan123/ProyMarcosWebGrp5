package com.gruponorte.mesaayudati.repository;

import com.gruponorte.mesaayudati.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/** Persistencia del catálogo de categorías. */
public interface CategoryRepository extends JpaRepository<Category, Long> {
    Optional<Category> findByNameIgnoreCase(String name);
    boolean existsByNameIgnoreCase(String name);
}