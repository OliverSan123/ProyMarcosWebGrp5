package com.gruponorte.mesaayudati.dto;

/** Token que el frontend enviará en Authorization: Bearer <token>. */
public record LoginResponse(String accessToken, String tokenType, long expiresInSeconds, String username,
                            String role) {
}