package com.example.ecommerce.domain.exception;

public class CorreoElectronicoInvalidoException extends ReglaDominioException {
    public CorreoElectronicoInvalidoException() {
        super("El correo electrónico no tiene un formato válido.");
    }
}
