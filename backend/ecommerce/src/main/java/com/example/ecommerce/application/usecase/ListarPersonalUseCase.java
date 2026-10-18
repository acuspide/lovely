package com.example.ecommerce.application.usecase;

import com.example.ecommerce.domain.entity.Usuario;
import com.example.ecommerce.domain.repository.UsuarioRepository;

import java.util.List;

/**
 * RF-01.2: cuentas del personal para el panel de la Administradora. Las
 * clientas no aparecen aquí porque gestionan su propia cuenta.
 */
public class ListarPersonalUseCase {

    private final UsuarioRepository usuarioRepository;

    public ListarPersonalUseCase(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    public List<Usuario> ejecutar() {
        return usuarioRepository.listarPersonal();
    }
}
