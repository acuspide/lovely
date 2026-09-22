package com.example.ecommerce.domain.exception;

public class DescripcionRequeridaException extends ReglaDominioException {
    public DescripcionRequeridaException() {
        super("La descripción del producto es obligatoria.");
    }
}
