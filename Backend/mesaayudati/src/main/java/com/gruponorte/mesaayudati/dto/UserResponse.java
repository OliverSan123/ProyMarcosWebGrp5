package com.gruponorte.mesaayudati.dto;

/** Representación pública de usuario, sin incluir nunca su hash de contraseña. */
public record UserResponse(Long id, String username, String email, String role, Long areaId,
                           String area, boolean enabled) {
}