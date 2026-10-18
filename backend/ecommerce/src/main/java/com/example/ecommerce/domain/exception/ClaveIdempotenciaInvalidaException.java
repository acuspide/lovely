package com.example.ecommerce.domain.exception;

/** RF-05.7: falta el identificador del intento de compra (o no tiene el formato esperado). */
public class ClaveIdempotenciaInvalidaException extends ReglaDominioException {
    public ClaveIdempotenciaInvalidaException() {
        super("No se pudo identificar el intento de compra. Recarga la página e intenta de nuevo.");
    }
}
