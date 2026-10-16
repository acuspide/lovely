package com.example.ecommerce.domain.exception;

public class ContrasenaDebilException extends ReglaDominioException {
    public ContrasenaDebilException() {
        super("La contraseña debe tener entre 8 y 20 caracteres, con al menos una letra, un número y un carácter especial (por ejemplo ! @ # * .).");
    }
}
