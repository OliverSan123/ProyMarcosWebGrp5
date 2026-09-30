package com.gruponorte.mesaayudati.dto;

import com.gruponorte.mesaayudati.entity.UserRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** Datos para crear o reemplazar una cuenta desde administración. */
public record UserRequest(
        @NotBlank @Size(max = 60) String username,
        @NotBlank @Email @Size(max = 160) String email,
        @NotBlank @Size(min = 8, max = 100) String password,
        @NotNull UserRole role,
        @NotNull Long areaId,
        boolean enabled) {
}