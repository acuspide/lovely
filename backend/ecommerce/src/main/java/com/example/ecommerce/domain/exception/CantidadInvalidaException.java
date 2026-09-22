package com.example.ecommerce.domain.exception;

public class CantidadInvalidaException extends ReglaDominioException {
    public CantidadInvalidaException() {
        super("La cantidad debe ser mayor que cero.");
    }
}
