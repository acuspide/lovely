package com.example.ecommerce.infrastructure.persistence;

import com.example.ecommerce.domain.entity.Factura;
import com.example.ecommerce.domain.exception.FacturaDuplicadaException;
import com.example.ecommerce.domain.repository.FacturaRepository;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Doble de pruebas del puerto FacturaRepository (tests unitarios de los casos
 * de uso). El adaptador de producción es FacturaRepositoryJpaAdapter (Oracle).
 */
public class FacturaRepositoryEnMemoria implements FacturaRepository {

    private final Map<Long, Factura> facturas = new LinkedHashMap<>();
    private final AtomicLong secuencia = new AtomicLong(0);

    @Override
    public Optional<Factura> buscarPorPedido(long pedidoId) {
        return facturas.values().stream().filter(f -> f.getPedidoId() == pedidoId).findFirst();
    }

    @Override
    public Factura guardar(Factura factura) {
        if (factura.getId() == 0) {
            if (buscarPorPedido(factura.getPedidoId()).isPresent()) {
                throw new FacturaDuplicadaException(null); // igual que la restricción UNIQUE de Oracle
            }
            factura = factura.conId(secuencia.incrementAndGet());
        }
        facturas.put(factura.getId(), factura);
        return factura;
    }

    public int cantidad() {
        return facturas.size();
    }
}
