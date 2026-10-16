package com.example.ecommerce.infrastructure.persistence.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface PedidoJpaRepository extends JpaRepository<PedidoJpaEntity, Long> {

    /** RF-05.7: pedido ya registrado para ese intento de compra. */
    Optional<PedidoJpaEntity> findByClienteIdAndClaveIdempotencia(Long clienteId, String claveIdempotencia);

    /** RN17 / RF-02.9: ¿algún pedido no cancelado incluye este producto? */
    @Query("""
            select case when count(p) > 0 then true else false end
            from PedidoJpaEntity p join p.detalles d
            where d.articuloId = :articuloId and p.estado <> 'CANCELADO'
            """)
    boolean existePedidoActivoConArticulo(@Param("articuloId") Long articuloId);
}
