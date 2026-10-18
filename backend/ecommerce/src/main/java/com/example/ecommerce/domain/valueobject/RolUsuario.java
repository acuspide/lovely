package com.example.ecommerce.domain.valueobject;

/**
 * Roles del sistema definidos en el SyRS (Sección 2.5, Características del Usuario):
 * Administradora, Asesora de Ventas, Encargada de Inventario y Cliente.
 */
public enum RolUsuario {
    CLIENTE,
    ASESORA_VENTAS,
    ENCARGADA_INVENTARIO,
    ADMINISTRADORA;

    /**
     * RF-01.2: el personal (todo rol distinto de Cliente) no se registra por su
     * cuenta; solo la Administradora crea y gestiona estas cuentas.
     */
    public boolean esPersonal() {
        return this != CLIENTE;
    }
}
