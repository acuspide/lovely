package com.example.ecommerce.infrastructure.persistence.jpa;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Repositorio Spring Data: Spring genera la implementación en tiempo de
 * ejecución. Es un detalle de infraestructura; el dominio nunca lo ve
 * directamente, solo a través de UsuarioRepositoryJpaAdapter.
 */
public interface UsuarioJpaRepository extends JpaRepository<UsuarioJpaEntity, Long> {
    Optional<UsuarioJpaEntity> findByEmail(String email);
    boolean existsByEmail(String email);
}
