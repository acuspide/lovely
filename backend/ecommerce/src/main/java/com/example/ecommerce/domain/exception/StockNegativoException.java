package com.example.ecommerce.domain.exception;

public class StockNegativoException extends ReglaDominioException {
    public StockNegativoException() {
        super("La cantidad disponible no puede ser negativa.");
    }
}
