package com.example.ecommerce.infrastructure.persistence;

import com.example.ecommerce.domain.entity.Usuario;
import com.example.ecommerce.domain.repository.UsuarioRepository;
import com.example.ecommerce.domain.valueobject.Email;
import com.example.ecommerce.infrastructure.persistence.jpa.UsuarioJpaEntity;
import com.example.ecommerce.infrastructure.persistence.jpa.UsuarioJpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Patrón: Adapter (puertos y adaptadores) — implementa el puerto de dominio
 * UsuarioRepository delegando en Spring Data/Oracle. Es la única clase que
 * conoce tanto el modelo de dominio como el de persistencia (vía
 * UsuarioMapper); el resto de la aplicación solo depende de la interfaz.
 */
@Repository
public class UsuarioRepositoryJpaAdapter implements UsuarioRepository {

    private final UsuarioJpaRepository jpaRepository;

    public UsuarioRepositoryJpaAdapter(UsuarioJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Optional<Usuario> obtenerPorId(long id) {
        return jpaRepository.findById(id).map(UsuarioMapper::aDominio);
    }

    @Override
    public Optional<Usuario> buscarPorEmail(Email email) {
        return jpaRepository.findByEmail(email.getValor()).map(UsuarioMapper::aDominio);
    }

    @Override
    public boolean existePorEmail(Email email) {
        return jpaRepository.existsByEmail(email.getValor());
    }

    @Override
    public List<Usuario> listarTodos() {
        return jpaRepository.findAll().stream()
                .map(UsuarioMapper::aDominio)
                .toList();
    }

    @Override
    public Usuario guardar(Usuario usuario) {
        UsuarioJpaEntity entidad = UsuarioMapper.aEntidad(usuario);
        UsuarioJpaEntity guardada = jpaRepository.save(entidad);
        return UsuarioMapper.aDominio(guardada);
    }
}
