package com.example.ecommerce.domain.exception;

/** CU-01, curso alterno A2: ya existe un producto con ese nombre. */
public class NombreArticuloDuplicadoException extends ReglaDominioException {
    public NombreArticuloDuplicadoException() {
        super("Ya existe un producto con ese nombre.");
    }
}
