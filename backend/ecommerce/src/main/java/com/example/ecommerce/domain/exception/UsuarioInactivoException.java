package com.example.ecommerce.domain.exception;

public class UsuarioInactivoException extends ReglaDominioException {
    public UsuarioInactivoException() {
        super("El usuario se encuentra inactivo.");
    }
}
