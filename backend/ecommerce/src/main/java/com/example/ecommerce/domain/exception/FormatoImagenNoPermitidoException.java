package com.example.ecommerce.domain.exception;

/** RF-02.4: el archivo está vacío o no es una imagen JPG, PNG ni WEBP. */
public class FormatoImagenNoPermitidoException extends ReglaDominioException {
    public FormatoImagenNoPermitidoException() {
        super("El archivo no es una imagen válida. Usa una imagen JPG, PNG o WEBP.");
    }
}
