package com.example.ecommerce.domain.exception;

public class UltimaAdministradoraActivaException extends ReglaDominioException {
    public UltimaAdministradoraActivaException() {
        super("No se puede desactivar ni cambiar el rol de la única Administradora activa del sistema.");
    }
}
