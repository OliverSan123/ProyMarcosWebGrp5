package com.gruponorte.mesaayudati.dto;

/** Forma común de devolver áreas, categorías y prioridades al frontend. */
public record CatalogResponse(Long id, String name, Integer slaHours) {
}