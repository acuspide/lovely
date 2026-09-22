package com.example.ecommerce.domain.exception;

public class PrecioRequeridoException extends ReglaDominioException {
    public PrecioRequeridoException() {
        super("El artículo debe tener un precio.");
    }
}
