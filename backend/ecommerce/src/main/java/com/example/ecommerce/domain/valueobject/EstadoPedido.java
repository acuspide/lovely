package com.example.ecommerce.domain.valueobject;

/**
 * Estados del pedido según el diagrama de estados del SyRS (Figura 13).
 * F-05 usa PENDIENTE (estado inicial, RF-05.1); los demás estados y sus
 * transiciones (En proceso, En camino, Entregado, Completado) llegan con F-07.
 */
public enum EstadoPedido {
    PENDIENTE,
    CONFIRMADO,
    CANCELADO;

    public boolean puedeTransicionarA(EstadoPedido nuevoEstado) {
        return switch (this) {
            case PENDIENTE -> nuevoEstado == CONFIRMADO || nuevoEstado == CANCELADO;
            case CONFIRMADO -> nuevoEstado == CANCELADO;
            case CANCELADO -> false;
        };
    }
}
