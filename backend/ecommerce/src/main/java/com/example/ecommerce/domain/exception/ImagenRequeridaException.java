package com.example.ecommerce.domain.exception;

public class ImagenRequeridaException extends ReglaDominioException {
    public ImagenRequeridaException() {
        super("La imagen del producto es obligatoria.");
    }
}
