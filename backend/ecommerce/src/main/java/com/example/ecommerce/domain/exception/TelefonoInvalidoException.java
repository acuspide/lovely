package com.example.ecommerce.domain.exception;

public class TelefonoInvalidoException extends ReglaDominioException {
    public TelefonoInvalidoException() {
        super("El teléfono debe tener 10 dígitos (por ejemplo, 3001234567).");
    }
}
