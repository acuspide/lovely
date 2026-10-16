package com.example.ecommerce.application.usecase;

import com.example.ecommerce.application.TokenProvider;
import com.example.ecommerce.domain.entity.Sesion;
import com.example.ecommerce.domain.entity.Usuario;
import com.example.ecommerce.domain.exception.TokenInvalidoException;
import com.example.ecommerce.domain.repository.SesionRepository;
import com.example.ecommerce.domain.repository.UsuarioRepository;
import com.example.ecommerce.domain.valueobject.RolUsuario;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;

/**
 * Se ejecuta en cada petición autenticada. Comprueba que:
 *  - el token sea auténtico,
 *  - su sesión no se haya cerrado (RF-01.7) ni vencido por inactividad (RF-01.6),
 *  - la cuenta siga activa (RF-01.2/RF-01.3).
 * Si todo está bien, la petición cuenta como actividad y la sesión se renueva.
 *
 * El rol se toma de la cuenta guardada y no del token, así que un cambio de
 * rol hecho por la Administradora se aplica desde la siguiente petición
 * (RNF-06).
 */
public class ValidarSesionUseCase {

    private final TokenProvider tokenProvider;
    private final SesionRepository sesionRepository;
    private final UsuarioRepository usuarioRepository;
    private final Duration inactividadMaxima;
    private final Clock reloj;

    public ValidarSesionUseCase(TokenProvider tokenProvider,
                                SesionRepository sesionRepository,
                                UsuarioRepository usuarioRepository,
                                Duration inactividadMaxima,
                                Clock reloj) {
        this.tokenProvider = tokenProvider;
        this.sesionRepository = sesionRepository;
        this.usuarioRepository = usuarioRepository;
        this.inactividadMaxima = inactividadMaxima;
        this.reloj = reloj;
    }

    public SesionActiva ejecutar(String token) {
        TokenProvider.DatosToken datos = tokenProvider.leer(token);
        Instant ahora = reloj.instant();

        Sesion sesion = sesionRepository.buscarPorId(datos.idSesion())
                .filter(encontrada -> encontrada.perteneceA(datos.idUsuario()))
                .filter(encontrada -> encontrada.estaVigente(ahora, inactividadMaxima))
                .orElseThrow(TokenInvalidoException::new);

        Usuario usuario = usuarioRepository.obtenerPorId(sesion.getUsuarioId())
                .filter(Usuario::isActivo)
                .orElseThrow(TokenInvalidoException::new);

        sesion.registrarActividad(ahora);
        sesionRepository.guardar(sesion);

        return new SesionActiva(
                sesion.getId(),
                usuario.getId(),
                usuario.getEmail().getValor(),
                usuario.getRol(),
                Duration.between(ahora, sesion.expiraEn(inactividadMaxima)));
    }

    /** @param vigencia tiempo que falta para que la sesión venza si no hay más actividad. */
    public record SesionActiva(String idSesion, long idUsuario, String email, RolUsuario rol, Duration vigencia) {
    }
}
