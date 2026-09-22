package com.example.ecommerce.domain.exception;

public class TransicionEstadoInvalidaException extends ReglaDominioException {
    public TransicionEstadoInvalidaException(String estadoActual, String estadoDestino) {
        super("No se puede pasar de " + estadoActual + " a " + estadoDestino + ".");
    }
}
