package com.example.ecommerce.domain.exception;

/** El pedido no existe o no pertenece a quien lo consulta. */
public class PedidoNoEncontradoException extends ReglaDominioException {
    public PedidoNoEncontradoException() {
        super("No encontramos ese pedido.");
    }
}
