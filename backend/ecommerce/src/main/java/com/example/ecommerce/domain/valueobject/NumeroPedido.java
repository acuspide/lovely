package com.example.ecommerce.domain.valueobject;

/**
 * Patrón: Value Object — número de pedido que ve la clienta (RF-05.2), por
 * ejemplo {@code LG-000123}.
 *
 * <p>Se forma a partir del consecutivo que asigna la base de datos (secuencia
 * PEDIDOS_SEQ), así que es único y crece de uno en uno. Se separa del id
 * técnico para que el formato pueda cambiar sin tocar la base de datos.</p>
 */
public record NumeroPedido(long consecutivo) {

    private static final String PREFIJO = "LG-";
    private static final int DIGITOS = 6;

    public NumeroPedido {
        if (consecutivo <= 0) {
            throw new IllegalArgumentException("El consecutivo del pedido debe ser positivo.");
        }
    }

    /** Texto para mostrar: "LG-" + el consecutivo con ceros a la izquierda. */
    public String valor() {
        return PREFIJO + String.format("%0" + DIGITOS + "d", consecutivo);
    }

    @Override
    public String toString() {
        return valor();
    }
}
