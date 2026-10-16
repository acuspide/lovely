package com.example.ecommerce.infrastructure.persistence;

import com.example.ecommerce.application.GestorTransacciones;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.function.Supplier;

/**
 * Adaptador del puerto GestorTransacciones con la gestión de transacciones de
 * Spring. Si la operación lanza una excepción, todo lo escrito se revierte.
 */
public class SpringGestorTransacciones implements GestorTransacciones {

    private final TransactionTemplate transactionTemplate;

    public SpringGestorTransacciones(TransactionTemplate transactionTemplate) {
        this.transactionTemplate = transactionTemplate;
    }

    @Override
    public <T> T enTransaccion(Supplier<T> operacion) {
        return transactionTemplate.execute(estado -> operacion.get());
    }
}
