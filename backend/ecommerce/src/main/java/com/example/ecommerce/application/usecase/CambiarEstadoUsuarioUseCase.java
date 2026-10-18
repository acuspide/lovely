package com.example.ecommerce.application.usecase;

import com.example.ecommerce.application.GestorTransacciones;
import com.example.ecommerce.domain.entity.Usuario;
import com.example.ecommerce.domain.exception.UsuarioNoEncontradoException;
import com.example.ecommerce.domain.repository.SesionRepository;
import com.example.ecommerce.domain.repository.UsuarioRepository;
import com.example.ecommerce.domain.service.ReglaAdministradoraActiva;

import java.time.Clock;

/**
 * RF-01.2: la Administradora activa o desactiva una cuenta del personal. Las
 * cuentas nunca se eliminan, solo se desactivan, para conservar su historial.
 *
 * Al desactivar se cierran también las sesiones abiertas de esa persona, para
 * que pierda el acceso de inmediato y no cuando venza su sesión.
 */
public class CambiarEstadoUsuarioUseCase {

    private final UsuarioRepository usuarioRepository;
    private final SesionRepository sesionRepository;
    private final ReglaAdministradoraActiva reglaAdministradoraActiva;
    private final GestorTransacciones transacciones;
    private final Clock reloj;

    public CambiarEstadoUsuarioUseCase(UsuarioRepository usuarioRepository,
                                       SesionRepository sesionRepository,
                                       ReglaAdministradoraActiva reglaAdministradoraActiva,
                                       GestorTransacciones transacciones,
                                       Clock reloj) {
        this.usuarioRepository = usuarioRepository;
        this.sesionRepository = sesionRepository;
        this.reglaAdministradoraActiva = reglaAdministradoraActiva;
        this.transacciones = transacciones;
        this.reloj = reloj;
    }

    public Usuario ejecutar(long idUsuario, boolean activo) {
        return transacciones.enTransaccion(() -> {
            Usuario cuenta = usuarioRepository.obtenerPorId(idUsuario)
                    .orElseThrow(UsuarioNoEncontradoException::new);
            cuenta.verificarQueEsPersonal();

            if (activo) {
                cuenta.activar();
                return usuarioRepository.guardar(cuenta);
            }

            reglaAdministradoraActiva.verificarQuePuedeDejarDeSerAdministradoraActiva(cuenta);
            cuenta.desactivar();
            Usuario desactivada = usuarioRepository.guardar(cuenta);
            sesionRepository.cerrarTodasDeUsuario(idUsuario, reloj.instant());
            return desactivada;
        });
    }
}
