package com.example.ecommerce.application.usecase;

import com.example.ecommerce.application.TokenProvider;
import com.example.ecommerce.domain.entity.Sesion;
import com.example.ecommerce.domain.entity.Usuario;
import com.example.ecommerce.domain.repository.SesionRepository;

import java.time.Clock;
import java.time.Duration;
import java.util.UUID;

/**
 * Patrón: Facade — orquesta autenticar las credenciales, abrir la sesión en el
 * servidor y emitir el token que la representa, para que el controlador REST
 * no tenga que coordinar esos pasos.
 */
public class IniciarSesionUseCase {

    private final AutenticarUsuarioUseCase autenticarUsuarioUseCase;
    private final SesionRepository sesionRepository;
    private final TokenProvider tokenProvider;
    private final Duration inactividadMaxima;
    private final Clock reloj;

    public IniciarSesionUseCase(AutenticarUsuarioUseCase autenticarUsuarioUseCase,
                                SesionRepository sesionRepository,
                                TokenProvider tokenProvider,
                                Duration inactividadMaxima,
                                Clock reloj) {
        this.autenticarUsuarioUseCase = autenticarUsuarioUseCase;
        this.sesionRepository = sesionRepository;
        this.tokenProvider = tokenProvider;
        this.inactividadMaxima = inactividadMaxima;
        this.reloj = reloj;
    }

    public SesionIniciada ejecutar(String email, String contrasenaPlano) {
        Usuario usuario = autenticarUsuarioUseCase.ejecutar(email, contrasenaPlano);

        Sesion sesion = sesionRepository.guardar(
                Sesion.iniciar(UUID.randomUUID().toString(), usuario.getId(), reloj.instant()));
        String token = tokenProvider.generarToken(usuario, sesion);

        return new SesionIniciada(usuario, token, inactividadMaxima);
    }

    /** @param vigencia tiempo que falta para que la sesión recién abierta venza si no hay actividad. */
    public record SesionIniciada(Usuario usuario, String token, Duration vigencia) {
    }
}
