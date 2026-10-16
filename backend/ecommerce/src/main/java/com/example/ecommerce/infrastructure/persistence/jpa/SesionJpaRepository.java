package com.example.ecommerce.infrastructure.persistence.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;

public interface SesionJpaRepository extends JpaRepository<SesionJpaEntity, String> {

    @Modifying
    @Query("""
            update SesionJpaEntity s set s.cerradaEn = :ahora
            where s.usuarioId = :usuarioId and s.cerradaEn is null
            """)
    int cerrarAbiertasDeUsuario(@Param("usuarioId") Long usuarioId, @Param("ahora") LocalDateTime ahora);

    @Modifying
    @Query("""
            update SesionJpaEntity s set s.cerradaEn = :ahora
            where s.usuarioId = :usuarioId and s.cerradaEn is null and s.id <> :idConservada
            """)
    int cerrarOtrasAbiertasDeUsuario(@Param("usuarioId") Long usuarioId,
                                     @Param("idConservada") String idConservada,
                                     @Param("ahora") LocalDateTime ahora);
}
