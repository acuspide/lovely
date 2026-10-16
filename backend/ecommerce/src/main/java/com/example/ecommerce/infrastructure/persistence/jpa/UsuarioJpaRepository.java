package com.example.ecommerce.infrastructure.persistence.jpa;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio Spring Data: Spring genera la implementación en tiempo de
 * ejecución. Es un detalle de infraestructura; el dominio nunca lo ve
 * directamente, solo a través de UsuarioRepositoryJpaAdapter.
 */
public interface UsuarioJpaRepository extends JpaRepository<UsuarioJpaEntity, Long> {
    Optional<UsuarioJpaEntity> findByEmail(String email);

    /** RF-01.4: SELECT ... FOR UPDATE; exige una transacción abierta. */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT u FROM UsuarioJpaEntity u WHERE u.email = :email")
    Optional<UsuarioJpaEntity> findByEmailParaActualizar(@Param("email") String email);

    boolean existsByEmail(String email);
    List<UsuarioJpaEntity> findByRolNotOrderByNombreAsc(UsuarioJpaEntity.RolJpa rol);
    long countByRolAndActivoTrue(UsuarioJpaEntity.RolJpa rol);
}
