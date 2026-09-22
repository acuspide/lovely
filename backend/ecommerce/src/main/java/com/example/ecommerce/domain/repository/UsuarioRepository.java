package com.example.ecommerce.domain.repository;

import com.example.ecommerce.domain.entity.Usuario;
import com.example.ecommerce.domain.valueobject.Email;

import java.util.List;
import java.util.Optional;

public interface UsuarioRepository {
    Optional<Usuario> obtenerPorId(long id);
    Optional<Usuario> buscarPorEmail(Email email);
    boolean existePorEmail(Email email);
    List<Usuario> listarTodos();

    /**
     * Persiste el usuario y devuelve la entidad resultante. Si {@code usuario}
     * llega "sin persistir" (id = 0), la implementación debe asignarle un id
     * real (secuencia/identity) y devolver la copia con {@link Usuario#conId(long)}.
     */
    Usuario guardar(Usuario usuario);
}