package com.example.ecommerce.application.usecase;

import com.example.ecommerce.domain.entity.Usuario;
import com.example.ecommerce.domain.repository.UsuarioRepository;

import java.util.List;

/**
 * Caso de uso de solo lectura para el panel de administración de usuarios
 * (accesible únicamente a ADMINISTRADORA, restricción aplicada en la capa web
 * con @PreAuthorize, no aquí).
 */
public class ListarUsuariosUseCase {

    private final UsuarioRepository repository;

    public ListarUsuariosUseCase(UsuarioRepository repository) {
        this.repository = repository;
    }

    public List<Usuario> ejecutar() {
        return repository.listarTodos();
    }
}
