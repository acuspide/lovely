package com.example.ecommerce.infrastructure.persistence;

import com.example.ecommerce.domain.entity.Articulo;
import com.example.ecommerce.domain.repository.ArticuloRepository;
import com.example.ecommerce.infrastructure.persistence.jpa.ArticuloJpaEntity;
import com.example.ecommerce.infrastructure.persistence.jpa.ArticuloJpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Patrón: Adapter — implementa el puerto de dominio ArticuloRepository
 * delegando en Spring Data/Oracle, igual que UsuarioRepositoryJpaAdapter en F-01.
 */
@Repository
public class ArticuloRepositoryJpaAdapter implements ArticuloRepository {

    private final ArticuloJpaRepository jpaRepository;

    public ArticuloRepositoryJpaAdapter(ArticuloJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Optional<Articulo> obtenerPorId(long id) {
        return jpaRepository.findById(id).map(ArticuloMapper::aDominio);
    }

    @Override
    public Optional<Articulo> buscarPorNombre(String nombre) {
        return jpaRepository.findByNombre(nombre).map(ArticuloMapper::aDominio);
    }

    @Override
    public List<Articulo> listarTodos() {
        return jpaRepository.findAll().stream()
                .map(ArticuloMapper::aDominio)
                .toList();
    }

    @Override
    public Articulo guardar(Articulo articulo) {
        ArticuloJpaEntity entidad = ArticuloMapper.aEntidad(articulo);
        ArticuloJpaEntity guardada = jpaRepository.save(entidad);
        return ArticuloMapper.aDominio(guardada);
    }

    @Override
    public void eliminar(long id) {
        jpaRepository.deleteById(id);
    }
}
