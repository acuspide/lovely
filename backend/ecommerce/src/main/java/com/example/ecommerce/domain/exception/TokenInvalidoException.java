package com.example.ecommerce.domain.exception;

public class TokenInvalidoException extends ReglaDominioException {
    public TokenInvalidoException() {
        super("La sesión no es válida o ha expirado.");
    }
}
