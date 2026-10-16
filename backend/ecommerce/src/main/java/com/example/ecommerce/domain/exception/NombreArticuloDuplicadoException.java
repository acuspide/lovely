package com.example.ecommerce.domain.exception;

/**
 * RF-02.3 / CU-01, curso alterno A2: ya existe un producto con ese nombre.
 * La comparación ignora mayúsculas, tildes y espacios repetidos (ver
 * {@link com.example.ecommerce.domain.valueobject.NombreArticulo#clave()}).
 */
public class NombreArticuloDuplicadoException extends ReglaDominioException {

    public NombreArticuloDuplicadoException(String nombreExistente) {
        super("Ya existe un producto llamado \"" + nombreExistente
                + "\". Usa un nombre diferente (no cuentan las mayúsculas, tildes ni espacios).");
    }
}
