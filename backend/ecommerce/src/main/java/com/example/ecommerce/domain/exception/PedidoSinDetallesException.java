package com.example.ecommerce.domain.exception;

public class PedidoSinDetallesException extends ReglaDominioException {
    public PedidoSinDetallesException() {
        super("El pedido debe tener al menos un detalle para poder confirmarse.");
    }
}
