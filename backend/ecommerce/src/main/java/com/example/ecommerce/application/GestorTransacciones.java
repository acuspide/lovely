package com.example.ecommerce.application;

import java.util.function.Supplier;

/**
 * Puerto para ejecutar varias escrituras como una sola unidad: o se guardan
 * todas o ninguna. Los casos de uso siguen siendo Java plano (sin
 * {@code @Transactional}); la infraestructura decide cómo se abre la
 * transacción.
 */
public interface GestorTransacciones {

    <T> T enTransaccion(Supplier<T> operacion);

    default void enTransaccion(Runnable operacion) {
        enTransaccion(() -> {
            operacion.run();
            return null;
        });
    }
}
