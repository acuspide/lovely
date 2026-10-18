package com.example.ecommerce.domain.valueobject;

/**
 * RF-07.5: quién cancela el pedido, según el diagrama de estados (Figura 13):
 * "cliente cancela" desde Pendiente y "administradora cancela" desde
 * Confirmado (la Administradora también puede cancelar uno Pendiente).
 */
public enum QuienCancela {

    CLIENTA,
    ADMINISTRADORA;

    public boolean puedeCancelarDesde(EstadoPedido estado) {
        return switch (this) {
            case CLIENTA -> estado == EstadoPedido.PENDIENTE;
            case ADMINISTRADORA -> estado.admiteCancelacion();
        };
    }
}
