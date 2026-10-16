package com.example.ecommerce.domain.service;

import com.example.ecommerce.domain.entity.Usuario;
import com.example.ecommerce.domain.exception.UltimaAdministradoraActivaException;
import com.example.ecommerce.domain.repository.UsuarioRepository;

/**
 * RF-01.2: siempre debe quedar al menos una Administradora activa; si no,
 * nadie podría volver a gestionar las cuentas del personal.
 *
 * Es un servicio de dominio porque la regla no depende de una sola cuenta,
 * sino del conjunto: hay que contar cuántas Administradoras activas quedan.
 */
public class ReglaAdministradoraActiva {

    private final UsuarioRepository usuarioRepository;

    public ReglaAdministradoraActiva(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    /**
     * Debe llamarse antes de que {@code usuario} deje de ser Administradora
     * activa, ya sea porque se desactiva o porque cambia de rol.
     */
    public void verificarQuePuedeDejarDeSerAdministradoraActiva(Usuario usuario) {
        if (usuario.esAdministradoraActiva() && usuarioRepository.contarAdministradorasActivas() <= 1) {
            throw new UltimaAdministradoraActivaException();
        }
    }
}
