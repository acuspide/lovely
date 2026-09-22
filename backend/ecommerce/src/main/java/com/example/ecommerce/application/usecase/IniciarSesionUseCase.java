package com.example.ecommerce.application.usecase;

import com.example.ecommerce.application.TokenProvider;
import com.example.ecommerce.domain.entity.Usuario;

/**
 * Patrón: Facade — orquesta dos colaboradores (autenticar credenciales y
 * emitir el token de sesión) detrás de una sola operación, para que el
 * controlador REST no tenga que conocer ni coordinar ambos pasos.
 */
public class IniciarSesionUseCase {

    private final AutenticarUsuarioUseCase autenticarUsuarioUseCase;
    private final TokenProvider tokenProvider;

    public IniciarSesionUseCase(AutenticarUsuarioUseCase autenticarUsuarioUseCase,
                                 TokenProvider tokenProvider) {
        this.autenticarUsuarioUseCase = autenticarUsuarioUseCase;
        this.tokenProvider = tokenProvider;
    }

    public SesionIniciada ejecutar(String email, String contrasenaPlano) {
        Usuario usuario = autenticarUsuarioUseCase.ejecutar(email, contrasenaPlano);
        String token = tokenProvider.generarToken(usuario);
        return new SesionIniciada(usuario, token, tokenProvider.minutosDeExpiracion());
    }

    public record SesionIniciada(Usuario usuario, String token, long minutosDeExpiracion) {
    }
}
