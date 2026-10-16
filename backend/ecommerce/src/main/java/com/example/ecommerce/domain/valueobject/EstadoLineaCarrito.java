package com.example.ecommerce.domain.valueobject;

/**
 * F-04: resultado de revisar una línea del carrito contra el inventario actual.
 */
public enum EstadoLineaCarrito {

    /** Hay unidades suficientes para la cantidad pedida. */
    DISPONIBLE,

    /** RF-04.2 / CU-02 A2: no alcanzan las unidades; la cantidad se bajó al stock disponible. */
    AJUSTADA,

    /** RF-04.1 / CU-02 A1 y A3: el producto no tiene unidades (stock 0). */
    AGOTADO,

    /** El producto ya no existe o la Administradora lo desactivó (RF-02.7). */
    NO_DISPONIBLE;

    /** Si la línea puede seguir en el pedido. */
    public boolean sePuedeComprar() {
        return this == DISPONIBLE || this == AJUSTADA;
    }
}
