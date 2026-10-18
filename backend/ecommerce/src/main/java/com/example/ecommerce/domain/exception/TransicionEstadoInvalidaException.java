package com.example.ecommerce.domain.exception;

/** RF-07.3 / RF-07.4: el cambio de estado no está permitido por el diagrama de estados. */
public class TransicionEstadoInvalidaException extends ReglaDominioException {
    public TransicionEstadoInvalidaException(String estadoActual, String estadoDestino) {
        super("No se puede pasar el pedido de " + estadoActual + " a " + estadoDestino + ".");
    }
}
