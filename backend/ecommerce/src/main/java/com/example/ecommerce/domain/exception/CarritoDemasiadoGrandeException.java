package com.example.ecommerce.domain.exception;

/** F-04: protección contra carritos desmedidos (muchos productos distintos en una sola revisión). */
public class CarritoDemasiadoGrandeException extends ReglaDominioException {
    public CarritoDemasiadoGrandeException(int maximo) {
        super("El carrito puede tener como máximo " + maximo + " productos distintos.");
    }
}
