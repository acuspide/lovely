package com.example.ecommerce.domain.exception;

/**
 * RN15: ya existe una factura para ese pedido (dos emisiones simultáneas).
 * La restricción UNIQUE de la base de datos es la última barrera.
 */
public class FacturaDuplicadaException extends ReglaDominioException {
    public FacturaDuplicadaException(Throwable causa) {
        super("Este pedido ya tiene factura.");
        if (causa != null) {
            initCause(causa);
        }
    }
}
