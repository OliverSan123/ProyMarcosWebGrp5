package com.gruponorte.mesaayudati.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Datos para crear o editar un elemento de catálogo. */
public record CatalogRequest(
        @NotBlank @Size(max = 100) String name,
        @Min(1) Integer slaHours) {
}