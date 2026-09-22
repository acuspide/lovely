package com.example.ecommerce.domain.exception;

public class CategoriaRequeridaException extends ReglaDominioException {

    public CategoriaRequeridaException() {
        super("El artículo debe pertenecer a una categoría.");
    }
}
