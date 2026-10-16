package com.example.ecommerce.domain.exception;

/** RN16 / RF-05.3: todo pedido debe tener al menos un producto. */
public class PedidoSinDetallesException extends ReglaDominioException {
    public PedidoSinDetallesException() {
        super("El pedido debe tener al menos un producto.");
    }
}
