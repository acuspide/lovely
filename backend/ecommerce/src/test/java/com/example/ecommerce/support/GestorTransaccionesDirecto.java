package com.example.ecommerce.support;

import com.example.ecommerce.application.GestorTransacciones;

import java.util.function.Supplier;

/** Doble del puerto GestorTransacciones: ejecuta la operación sin transacción. */
public class GestorTransaccionesDirecto implements GestorTransacciones {

    @Override
    public <T> T enTransaccion(Supplier<T> operacion) {
        return operacion.get();
    }
}
