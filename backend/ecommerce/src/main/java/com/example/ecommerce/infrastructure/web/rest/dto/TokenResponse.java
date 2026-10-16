package com.example.ecommerce.infrastructure.web.rest.dto;

import com.example.ecommerce.application.usecase.IniciarSesionUseCase.SesionIniciada;

/**
 * Respuesta del inicio de sesión. {@code segundosParaExpirar} indica cuánto
 * falta para que la sesión venza si no hay actividad (RF-01.6). Se envía como
 * duración y no como hora absoluta para que el aviso de "Seguir conectado" no
 * dependa de que el reloj del equipo de la usuaria esté en hora.
 */
public record TokenResponse(String token, String tipo, long segundosParaExpirar, UsuarioResponse usuario) {

    public static TokenResponse desde(SesionIniciada sesion) {
        return new TokenResponse(
                sesion.token(),
                "Bearer",
                sesion.vigencia().toSeconds(),
                UsuarioResponse.desde(sesion.usuario()));
    }
}
