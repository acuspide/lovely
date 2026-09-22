package com.example.ecommerce.domain.exception;

public class PrecioInvalidoException extends ReglaDominioException {
    public PrecioInvalidoException() {
        super("El precio no puede ser nulo ni negativo.");
    }
}
