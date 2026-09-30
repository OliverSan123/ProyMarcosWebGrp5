package com.gruponorte.mesaayudati.dto;

import jakarta.validation.constraints.NotBlank;

/** Credenciales recibidas por el endpoint de inicio de sesión. */
public record LoginRequest(
        @NotBlank String username,
        @NotBlank String password) {
}