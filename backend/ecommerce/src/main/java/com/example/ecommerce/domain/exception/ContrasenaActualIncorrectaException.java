package com.example.ecommerce.domain.exception;

public class ContrasenaActualIncorrectaException extends ReglaDominioException {
    public ContrasenaActualIncorrectaException() {
        super("La contraseña actual no es correcta.");
    }
}
