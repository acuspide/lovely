package com.example.ecommerce.domain.exception;

public class ArticuloNoEncontradoEnCarritoException extends ReglaDominioException {
    public ArticuloNoEncontradoEnCarritoException() {
        super("El articulo no se encuentra en el carrito.");
    }
}
