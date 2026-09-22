package com.example.ecommerce.application;

import com.example.ecommerce.domain.entity.Usuario;

/**
 * Puerto de aplicación para emitir y validar el token de sesión del usuario.
 * El caso de uso que lo usa (IniciarSesionUseCase) no sabe si detrás hay JWT,
 * un token opaco u otro esquema: eso es una decisión de infraestructura
 * (JwtTokenProvider), lo que permite cambiarla sin tocar la capa de
 * aplicación (OCP/DIP).
 */
public interface TokenProvider {

    /**
     * Genera un token de sesión para el usuario autenticado.
     */
    String generarToken(Usuario usuario);

    /**
     * Minutos de validez del token, usados también para informar al cliente
     * cuándo debe re-autenticarse (RF-01: sesión expira tras 30 min).
     */
    long minutosDeExpiracion();

    /**
     * Valida la firma y vigencia del token y devuelve los datos que contiene.
     * Lanza {@link com.example.ecommerce.domain.exception.TokenInvalidoException}
     * si el token no es válido o ya expiró.
     */
    DatosToken validar(String token);

    record DatosToken(long idUsuario, String email, String rol) {
    }
}
