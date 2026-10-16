package com.example.ecommerce.domain.exception;

public class RolRequeridoException extends ReglaDominioException {
    public RolRequeridoException() {
        super("El usuario debe tener un rol asignado (Cliente, Asesora de Ventas, Encargada de Inventario o Administradora).");
    }
}