package com.example.ecommerce.infrastructure.persistence;

import com.example.ecommerce.domain.entity.Usuario;
import com.example.ecommerce.domain.repository.UsuarioRepository;
import com.example.ecommerce.domain.valueobject.Email;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Doble de pruebas del puerto UsuarioRepository (usado en los tests unitarios
 * de los casos de uso). El adaptador real de producción es
 * UsuarioRepositoryJpaAdapter (Oracle). Ambos son intercambiables detrás del
 * mismo puerto: Liskov Substitution en la práctica.
 */
public class UsuarioRepositoryEnMemoria implements UsuarioRepository {

    private final Map<Long, Usuario> usuarios = new HashMap<>();
    private final AtomicLong secuenciaId = new AtomicLong(0);

    @Override
    public Optional<Usuario> obtenerPorId(long id) {
        return Optional.ofNullable(usuarios.get(id));
    }

    @Override
    public Optional<Usuario> buscarPorEmail(Email email) {
        return usuarios.values().stream()
                .filter(usuario -> usuario.getEmail().equals(email))
                .findFirst();
    }

    @Override
    public boolean existePorEmail(Email email) {
        return usuarios.values().stream()
                .anyMatch(usuario -> usuario.getEmail().equals(email));
    }

    @Override
    public List<Usuario> listarTodos() {
        return List.copyOf(usuarios.values());
    }

    @Override
    public Usuario guardar(Usuario usuario) {
        Usuario aGuardar = usuario.getId() == 0
                ? usuario.conId(secuenciaId.incrementAndGet())
                : usuario;
        usuarios.put(aGuardar.getId(), aGuardar);
        return aGuardar;
    }
}