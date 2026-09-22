package com.example.ecommerce.domain.exception;

public class MarcaRequeridaException extends ReglaDominioException {
    public MarcaRequeridaException() {
        super("La marca es obligatoria.");
    }
}
