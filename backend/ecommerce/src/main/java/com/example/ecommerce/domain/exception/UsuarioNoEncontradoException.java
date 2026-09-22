package com.example.ecommerce.domain.exception;

public class UsuarioNoEncontradoException extends ReglaDominioException {
    public UsuarioNoEncontradoException() {
        super("El usuario solicitado no existe.");
    }
}
