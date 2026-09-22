package com.example.ecommerce.domain.exception;

public class ArticuloNoRegistradoException extends ReglaDominioException {
    public ArticuloNoRegistradoException() {
        super("No existe un articulo registrado con ese id.");
    }
}
