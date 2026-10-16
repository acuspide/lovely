package com.example.ecommerce.domain.exception;

/** RF-07.5: la cancelación exige un motivo (y no demasiado largo). */
public class MotivoCancelacionInvalidoException extends ReglaDominioException {
    public MotivoCancelacionInvalidoException(String mensaje) {
        super(mensaje);
    }
}
