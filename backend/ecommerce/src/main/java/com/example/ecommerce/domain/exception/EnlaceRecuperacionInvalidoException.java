package com.example.ecommerce.domain.exception;

public class EnlaceRecuperacionInvalidoException extends ReglaDominioException {
    public EnlaceRecuperacionInvalidoException() {
        super("El enlace de recuperación no es válido, ya fue usado o venció. Solicita uno nuevo.");
    }
}
