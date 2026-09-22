package com.example.ecommerce.domain.exception;

public class CredencialesInvalidasException extends ReglaDominioException {
    public CredencialesInvalidasException() {
        super("Correo o contraseña incorrectos.");
    }
}