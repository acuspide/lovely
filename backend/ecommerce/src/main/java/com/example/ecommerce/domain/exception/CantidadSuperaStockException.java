package com.example.ecommerce.domain.exception;

public class CantidadSuperaStockException extends ReglaDominioException {
    public CantidadSuperaStockException() {
        super("La cantidad solicitada supera el stock disponible del articulo.");
    }
}
