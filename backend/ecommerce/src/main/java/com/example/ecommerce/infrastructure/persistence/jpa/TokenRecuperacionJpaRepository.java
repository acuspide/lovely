package com.example.ecommerce.infrastructure.persistence.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;

public interface TokenRecuperacionJpaRepository extends JpaRepository<TokenRecuperacionJpaEntity, Long> {

    Optional<TokenRecuperacionJpaEntity> findByHuella(String huella);

    @Modifying
    @Query("""
            update TokenRecuperacionJpaEntity t set t.usadoEn = :ahora
            where t.usuarioId = :usuarioId and t.usadoEn is null
            """)
    int anularPendientesDeUsuario(@Param("usuarioId") Long usuarioId, @Param("ahora") LocalDateTime ahora);
}
