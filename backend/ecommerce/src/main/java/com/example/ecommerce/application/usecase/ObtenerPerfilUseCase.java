package com.example.ecommerce.application.usecase;

import com.example.ecommerce.domain.entity.Usuario;
import com.example.ecommerce.domain.exception.UsuarioNoEncontradoException;
import com.example.ecommerce.domain.repository.UsuarioRepository;

/**
 * Datos de la propia cuenta para la pantalla de perfil (IU-07).
 */
public class ObtenerPerfilUseCase {

    private final UsuarioRepository usuarioRepository;

    public ObtenerPerfilUseCase(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    public Usuario ejecutar(long idUsuario) {
        return usuarioRepository.obtenerPorId(idUsuario)
                .orElseThrow(UsuarioNoEncontradoException::new);
    }
}
