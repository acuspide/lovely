package com.example.ecommerce.domain.exception;

/**
 * RN11 / RF-09.6: un pedido no pasa a Confirmado (ni recibe factura) por un
 * simple cambio de estado; antes hay que registrar la confirmación del pago.
 */
public class PagoNoConfirmadoException extends ReglaDominioException {
    public PagoNoConfirmadoException() {
        super("Para confirmar el pedido, registra primero la confirmación del pago "
                + "(medio, referencia, monto y fecha de recepción).");
    }
}
