package com.example.ecommerce.domain.exception;

/**
 * RF-05.7: dos envíos del mismo intento de compra llegaron a la vez; la base
 * de datos aceptó solo el primero. El caso de uso responde con ese pedido.
 */
public class PedidoDuplicadoException extends RuntimeException {
    public PedidoDuplicadoException(Throwable causa) {
        super("Ya existe un pedido para este intento de compra.", causa);
    }
}
