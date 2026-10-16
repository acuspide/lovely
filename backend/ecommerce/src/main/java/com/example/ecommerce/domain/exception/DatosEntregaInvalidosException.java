package com.example.ecommerce.domain.exception;

/** RF-04.6: falta un dato de contacto o de entrega, o no es válido. */
public class DatosEntregaInvalidosException extends ReglaDominioException {
    public DatosEntregaInvalidosException(String mensaje) {
        super(mensaje);
    }
}
