package com.example.ecommerce.domain.exception;

public class TiendaRequeridaException extends ReglaDominioException{
    public TiendaRequeridaException() {
        super("La tienda es obligatoria.");
    }
}
