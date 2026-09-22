package com.example.ecommerce.domain.exception;

public class ContrasenasNoCoincidenException extends ReglaDominioException {
    public ContrasenasNoCoincidenException() {
        super("Las contraseñas no coinciden.");
    }
}
