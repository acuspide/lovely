package com.example.ecommerce.domain.valueobject;

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
