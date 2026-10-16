package com.example.ecommerce.domain.exception;

/**
 * RF-08.1: el pedido todavía no tiene factura (sigue Pendiente) o se canceló
 * antes de confirmarse, así que nunca la tendrá.
 */
public class FacturaNoDisponibleException extends ReglaDominioException {
    public FacturaNoDisponibleException(String mensaje) {
        super(mensaje);
    }
}
