package com.example.ecommerce.infrastructure.persistence.jpa;

import jakarta.persistence.LockModeType;
import jakarta.persistence.QueryHint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PedidoJpaRepository extends JpaRepository<PedidoJpaEntity, Long> {

    /** RF-05.7: pedido ya registrado para ese intento de compra. */
    Optional<PedidoJpaEntity> findByClienteIdAndClaveIdempotencia(Long clienteId, String claveIdempotencia);

    /** F-07: bloquea el pedido mientras se cambia su estado (máximo 5 s de espera). */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @QueryHints(@QueryHint(name = "jakarta.persistence.lock.timeout", value = "5000"))
    @Query("select p from PedidoJpaEntity p where p.id = :id")
    Optional<PedidoJpaEntity> findByIdParaActualizar(@Param("id") Long id);

    /** RF-07.1: todos los pedidos, del más reciente al más antiguo. */
    List<PedidoJpaEntity> findAllByOrderByCreadoEnDescIdDesc();

    /** RF-07.1: pedidos de un estado, del más reciente al más antiguo. */
    List<PedidoJpaEntity> findByEstadoOrderByCreadoEnDescIdDesc(String estado);

    /** RF-07.8: pedidos de una clienta, del más reciente al más antiguo. */
    List<PedidoJpaEntity> findByClienteIdOrderByCreadoEnDescIdDesc(Long clienteId);

    /** RN17 / RF-02.9: ¿algún pedido no cancelado incluye este producto? */
    @Query("""
            select case when count(p) > 0 then true else false end
            from PedidoJpaEntity p join p.detalles d
            where d.articuloId = :articuloId and p.estado <> 'CANCELADO'
            """)
    boolean existePedidoActivoConArticulo(@Param("articuloId") Long articuloId);
}
