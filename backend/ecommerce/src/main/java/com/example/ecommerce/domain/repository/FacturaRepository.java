package com.example.ecommerce.domain.repository;

import com.example.ecommerce.domain.entity.Factura;

import java.util.Optional;

/**
 * Puerto de salida (arquitectura hexagonal) para las facturas (F-08). En
 * producción lo implementa el adaptador de Oracle/JPA; en las pruebas, uno en
 * memoria.
 */
public interface FacturaRepository {

    /** RN15: la factura de un pedido, si ya se emitió. */
    Optional<Factura> buscarPorPedido(long pedidoId);

    /**
     * Guarda la factura y devuelve la versión guardada (con número, si era nueva).
     *
     * @throws com.example.ecommerce.domain.exception.FacturaDuplicadaException si el pedido ya tenía factura
     */
    Factura guardar(Factura factura);
}
