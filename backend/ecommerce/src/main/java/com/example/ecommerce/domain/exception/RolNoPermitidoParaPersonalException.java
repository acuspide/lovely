package com.example.ecommerce.domain.exception;

public class RolNoPermitidoParaPersonalException extends ReglaDominioException {
    public RolNoPermitidoParaPersonalException() {
        super("Solo se pueden asignar los roles de Asesora de Ventas, Encargada de Inventario o Administradora al personal.");
    }
}
