package com.example.ecommerce.domain.exception;

public class ContrasenaRepetidaException extends ReglaDominioException {
    public ContrasenaRepetidaException() {
        super("La nueva contraseña debe ser distinta de la actual.");
    }
}
