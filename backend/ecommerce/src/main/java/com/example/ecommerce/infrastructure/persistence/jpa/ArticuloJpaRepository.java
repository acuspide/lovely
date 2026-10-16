package com.example.ecommerce.infrastructure.persistence.jpa;

import jakarta.persistence.LockModeType;
import jakarta.persistence.QueryHint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ArticuloJpaRepository extends JpaRepository<ArticuloJpaEntity, Long> {

    /** RF-02.3: búsqueda por la clave normalizada del nombre (columna NOMBRE_CLAVE). */
    Optional<ArticuloJpaEntity> findByNombreClave(String nombreClave);

    /**
     * F-06 / RF-06.5: SELECT ... FOR UPDATE. Bloquea la fila del producto hasta
     * que termine la transacción del pedido. Si otra compra la tiene bloqueada,
     * espera como máximo 5 segundos (luego se le pide a la clienta reintentar).
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @QueryHints(@QueryHint(name = "jakarta.persistence.lock.timeout", value = "5000"))
    @Query("SELECT a FROM ArticuloJpaEntity a WHERE a.id = :id")
    Optional<ArticuloJpaEntity> findByIdParaActualizar(@Param("id") Long id);
}
