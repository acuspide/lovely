package com.example.ecommerce.domain.exception;

public class HabeasDataNoAceptadoException extends ReglaDominioException {
    public HabeasDataNoAceptadoException() {
        super("Debes aceptar la política de tratamiento de datos personales (Ley 1581 de 2012) para crear tu cuenta.");
    }
}
