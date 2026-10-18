package com.example.ecommerce.domain.exception;

/**
 * RF-02.7: el producto está desactivado, así que no se muestra en el catálogo
 * ni se puede agregar al carrito.
 */
public class ArticuloInactivoException extends ReglaDominioException {
    public ArticuloInactivoException() {
        super("Este producto no está disponible en este momento.");
    }
}
