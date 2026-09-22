package com.example.ecommerce.domain.exception;

public class CorreoElectronicoDuplicadoException extends ReglaDominioException {
    public CorreoElectronicoDuplicadoException() {
        super("Ya existe un usuario registrado con este correo electrónico.");
    }
}
