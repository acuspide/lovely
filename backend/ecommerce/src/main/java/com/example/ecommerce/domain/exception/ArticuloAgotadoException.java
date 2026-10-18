package com.example.ecommerce.domain.exception;

/**
 * RF-03.4 / RN01 / RN12: el producto no tiene unidades (stock 0), así que no
 * se puede agregar al carrito ni vender.
 */
public class ArticuloAgotadoException extends ReglaDominioException {
    public ArticuloAgotadoException() {
        super("Este producto está agotado. Vuelve pronto: estamos reponiendo unidades.");
    }
}
