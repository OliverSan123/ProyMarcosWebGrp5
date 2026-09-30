package com.gruponorte.mesaayudati.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Comentario de seguimiento asociado a un ticket. */
public record CommentRequest(@NotBlank @Size(max = 3000) String comment) {
}