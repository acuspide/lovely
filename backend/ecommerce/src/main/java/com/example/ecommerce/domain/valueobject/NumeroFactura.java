package com.example.ecommerce.domain.valueobject;

/**
 * RF-08.2 / RN06: número visible de la factura, consecutivo y único
 * ("FV-000001", "FV-000002"…). Sale del id que asigna la secuencia de la
 * base de datos, igual que {@link NumeroPedido}.
 */
public record NumeroFactura(long consecutivo) {

    public NumeroFactura {
        if (consecutivo <= 0) {
            throw new IllegalArgumentException("La factura todavía no tiene número asignado.");
        }
    }

    public String valor() {
        return "FV-%06d".formatted(consecutivo);
    }

    @Override
    public String toString() {
        return valor();
    }
}
