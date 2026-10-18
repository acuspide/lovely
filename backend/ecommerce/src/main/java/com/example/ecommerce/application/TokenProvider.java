package com.example.ecommerce.application;

import com.example.ecommerce.domain.entity.Sesion;
import com.example.ecommerce.domain.entity.Usuario;

/**
 * Puerto de aplicación para emitir y leer el token que representa una sesión.
 * Los casos de uso no saben si detrás hay JWT, un token opaco u otro esquema:
 * eso es una decisión de infraestructura (JwtTokenProvider), lo que permite
 * cambiarla sin tocar la capa de aplicación (OCP/DIP).
 *
 * El token solo prueba que lo emitió este sistema; si la sesión sigue vigente
 * (no se cerró ni venció por inactividad) lo decide ValidarSesionUseCase
 * consultando la sesión guardada.
 */
public interface TokenProvider {

    String generarToken(Usuario usuario, Sesion sesion);

    /**
     * Verifica la firma y la vigencia máxima del token y devuelve los datos
     * que contiene. Lanza
     * {@link com.example.ecommerce.domain.exception.TokenInvalidoException}
     * si el token fue alterado o ya venció.
     */
    DatosToken leer(String token);

    record DatosToken(String idSesion, long idUsuario) {
    }
}
