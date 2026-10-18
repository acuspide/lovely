package com.example.ecommerce.domain.exception;

/** RF-02.4: la imagen supera el tamaño máximo permitido. */
public class ImagenDemasiadoGrandeException extends ReglaDominioException {
    public ImagenDemasiadoGrandeException(int tamanoMaximoMb) {
        super("La imagen pesa demasiado. El tamaño máximo es " + tamanoMaximoMb + " MB.");
    }
}
