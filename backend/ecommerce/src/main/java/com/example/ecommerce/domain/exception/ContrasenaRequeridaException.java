package com.example.ecommerce.domain.exception;

public class ContrasenaRequeridaException extends ReglaDominioException {
    public ContrasenaRequeridaException() {
        super("La contraseña es obligatoria.");
    }
}