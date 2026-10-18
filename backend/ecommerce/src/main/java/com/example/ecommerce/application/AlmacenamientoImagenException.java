package com.example.ecommerce.application;

/**
 * CU-01, curso alterno A4: falló el almacenamiento de la imagen por un motivo
 * técnico (no por una regla de negocio). La interfaz ofrece reintentar o
 * continuar sin imagen.
 */
public class AlmacenamientoImagenException extends RuntimeException {
    public AlmacenamientoImagenException(Throwable causa) {
        super("No se pudo guardar la imagen. Intenta de nuevo o continúa sin imagen.", causa);
    }
}
