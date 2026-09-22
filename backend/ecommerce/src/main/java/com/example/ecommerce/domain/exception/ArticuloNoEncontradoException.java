package com.example.ecommerce.domain.exception;

public class ArticuloNoEncontradoException extends ReglaDominioException {
    public ArticuloNoEncontradoException() {
        super("El producto solicitado no existe.");
    }
}
