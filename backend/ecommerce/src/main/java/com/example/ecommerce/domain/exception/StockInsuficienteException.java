package com.example.ecommerce.domain.exception;

/**
 * RF-05.4 / RN09: no hay unidades suficientes para la cantidad pedida, así
 * que el pedido no se registra y se informa a la clienta.
 */
public class StockInsuficienteException extends ReglaDominioException {
    public StockInsuficienteException(String nombreArticulo, int disponible) {
        super(disponible == 0
                ? "«" + nombreArticulo + "» se agotó. Revisa tu carrito antes de confirmar."
                : "Solo quedan " + disponible + " unidades de «" + nombreArticulo
                        + "». Revisa tu carrito antes de confirmar.");
    }
}
