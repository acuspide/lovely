package com.example.ecommerce.domain.exception;

/** RF-09.2 a RF-09.4: los datos de la confirmación del pago no son válidos. */
public class PagoInvalidoException extends ReglaDominioException {
    public PagoInvalidoException(String mensaje) {
        super(mensaje);
    }
}
