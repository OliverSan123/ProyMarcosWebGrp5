package com.gruponorte.mesaayudati.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Datos para crear o editar un elemento de catálogo. */
public record CatalogRequest(
        @NotBlank(message = "El nombre es obligatorio") @Size(max = 100, message = "El nombre admite hasta 100 caracteres") String name,
        @Min(value = 1, message = "El SLA debe ser mayor que cero") Integer slaHours) {
}