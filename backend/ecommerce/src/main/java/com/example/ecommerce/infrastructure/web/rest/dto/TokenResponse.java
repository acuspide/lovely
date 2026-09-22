package com.example.ecommerce.infrastructure.web.rest.dto;

import com.example.ecommerce.application.usecase.IniciarSesionUseCase.SesionIniciada;

public record TokenResponse(String token, String tipo, long expiraEnMinutos, UsuarioResponse usuario) {

    public static TokenResponse desde(SesionIniciada sesion) {
        return new TokenResponse(
                sesion.token(),
                "Bearer",
                sesion.minutosDeExpiracion(),
                UsuarioResponse.desde(sesion.usuario()));
    }
}
