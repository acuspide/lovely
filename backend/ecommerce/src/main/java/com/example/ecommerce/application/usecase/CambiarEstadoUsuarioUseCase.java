package com.example.ecommerce.application.usecase;

import com.example.ecommerce.domain.entity.Usuario;
import com.example.ecommerce.domain.exception.UsuarioNoEncontradoException;
import com.example.ecommerce.domain.repository.UsuarioRepository;

/**
 * Activa o desactiva una cuenta de usuario. Usado por el panel de
 * administración (solo ADMINISTRADORA, restricción aplicada en la capa web).
 */
public class CambiarEstadoUsuarioUseCase {

    private final UsuarioRepository repository;

    public CambiarEstadoUsuarioUseCase(UsuarioRepository repository) {
        this.repository = repository;
    }

    public Usuario ejecutar(long idUsuario, boolean activo) {
        Usuario usuario = repository.obtenerPorId(idUsuario)
                .orElseThrow(UsuarioNoEncontradoException::new);

        if (activo) {
            usuario.activar();
        } else {
            usuario.desactivar();
        }

        return repository.guardar(usuario);
    }
}
