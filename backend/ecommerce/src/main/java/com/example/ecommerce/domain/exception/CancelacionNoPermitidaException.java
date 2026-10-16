package com.example.ecommerce.domain.exception;

/** RF-07.5: quien intenta cancelar no puede hacerlo en el estado actual del pedido. */
public class CancelacionNoPermitidaException extends ReglaDominioException {
    public CancelacionNoPermitidaException(String mensaje) {
        super(mensaje);
    }
}
