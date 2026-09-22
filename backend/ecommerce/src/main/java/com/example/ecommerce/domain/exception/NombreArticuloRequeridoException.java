package com.example.ecommerce.domain.exception;

public class NombreArticuloRequeridoException extends ReglaDominioException {
    public NombreArticuloRequeridoException() {
        super("El nombre del artículo es obligatorio.");
    }
}
