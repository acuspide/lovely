package com.example.ecommerce.infrastructure.persistence;

import com.example.ecommerce.domain.entity.Usuario;
import com.example.ecommerce.domain.repository.UsuarioRepository;
import com.example.ecommerce.domain.valueobject.Email;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class UsuarioRepositoryEnMemoria implements UsuarioRepository {

    private final Map<Long, Usuario> usuarios = new HashMap<>();

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
    public void guardar(Usuario usuario) {
        usuarios.put(usuario.getId(), usuario);
    }
}