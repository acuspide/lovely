package com.example.ecommerce.domain.exception;

public class FechaVencimientoRequeridaException extends ReglaDominioException {
    public FechaVencimientoRequeridaException() {
        super("La fecha de vencimiento es obligatoria.");
    }
}
