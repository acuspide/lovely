package com.example.ecommerce.application.usecase;

import com.example.ecommerce.domain.entity.Factura;
import com.example.ecommerce.domain.repository.FacturaRepository;

import java.util.Optional;

/**
 * F-08: ¿el pedido ya tiene factura? Lo usan las pantallas del pedido para
 * mostrar su número y el botón de descarga. No emite nada.
 */
public class ConsultarFacturaUseCase {

    private final FacturaRepository facturaRepository;

    public ConsultarFacturaUseCase(FacturaRepository facturaRepository) {
        this.facturaRepository = facturaRepository;
    }

    public Optional<Factura> ejecutar(long pedidoId) {
        return facturaRepository.buscarPorPedido(pedidoId);
    }
}
