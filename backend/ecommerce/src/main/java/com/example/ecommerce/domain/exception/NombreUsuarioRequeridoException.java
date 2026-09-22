package com.example.ecommerce.domain.exception;

public class NombreUsuarioRequeridoException extends ReglaDominioException {
    public NombreUsuarioRequeridoException() {
        super("El nombre del usuario es obligatorio.");
    }
}