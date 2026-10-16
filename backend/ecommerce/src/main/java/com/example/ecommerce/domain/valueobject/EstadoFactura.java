package com.example.ecommerce.domain.valueobject;

/**
 * RF-08.6: una factura emitida se anula si su pedido se cancela. Nunca se
 * borra: conserva su número para que el consecutivo no tenga huecos (RN06).
 */
public enum EstadoFactura {
    EMITIDA,
    ANULADA
}
