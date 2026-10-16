package com.example.ecommerce.domain.valueobject;

/**
 * RF-04.6 / CU-02 paso 8: cómo recibe la clienta su pedido.
 */
public enum TipoEntrega {

    /** Pasa a recogerlo a la tienda en Armenia; no necesita dirección. */
    RETIRO_EN_TIENDA(false),

    /** Domicilio dentro de la ciudad. */
    DOMICILIO(true),

    /** Envío por transportadora a otra ciudad. */
    ENVIO(true);

    private final boolean requiereDireccion;

    TipoEntrega(boolean requiereDireccion) {
        this.requiereDireccion = requiereDireccion;
    }

    public boolean requiereDireccion() {
        return requiereDireccion;
    }
}
