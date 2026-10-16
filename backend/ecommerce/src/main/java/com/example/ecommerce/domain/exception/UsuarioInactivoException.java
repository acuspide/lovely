package com.example.ecommerce.domain.exception;

public class UsuarioInactivoException extends ReglaDominioException {
    public UsuarioInactivoException() {
        super("Tu cuenta está desactivada. Comunícate con la administradora de Lovely Girl.");
    }
}
