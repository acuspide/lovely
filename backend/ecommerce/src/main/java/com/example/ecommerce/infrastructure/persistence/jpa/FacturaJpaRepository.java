package com.example.ecommerce.infrastructure.persistence.jpa;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface FacturaJpaRepository extends JpaRepository<FacturaJpaEntity, Long> {

    /** RN15: la factura de un pedido. */
    Optional<FacturaJpaEntity> findByPedidoId(Long pedidoId);
}
