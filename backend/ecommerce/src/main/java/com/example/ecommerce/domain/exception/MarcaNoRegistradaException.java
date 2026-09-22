package com.example.ecommerce.domain.exception;

public class MarcaNoRegistradaException extends ReglaDominioException {
    public MarcaNoRegistradaException() {
        super("El artículo debe tener una marca registrada.");
    }
}
