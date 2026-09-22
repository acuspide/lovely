package com.example.ecommerce.domain.valueobject;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class EstadoPedidoTest {

    @Test
    void unPedidoPendienteDebePoderConfirmarse() {

        // Arrange
        EstadoPedido estado = EstadoPedido.PENDIENTE;

        // Act
        boolean puedeConfirmarse = estado.puedeTransicionarA(EstadoPedido.CONFIRMADO);

        // Assert
        assertTrue(puedeConfirmarse);
    }

    @Test
    void unPedidoCanceladoNuncaDebeVolverAConfirmado() {

        // Arrange
        EstadoPedido estado = EstadoPedido.CANCELADO;

        // Act
        boolean puedeConfirmarse = estado.puedeTransicionarA(EstadoPedido.CONFIRMADO);

        // Assert
        assertFalse(puedeConfirmarse);
    }

    @Test
    void unPedidoCanceladoNoDebePoderTransicionarANingunEstado() {

        // Arrange
        EstadoPedido estado = EstadoPedido.CANCELADO;

        // Act
        // No necesitamos una accion adicional, se prueban ambas
        // transiciones directamente en el assert.

        // Assert
        assertFalse(estado.puedeTransicionarA(EstadoPedido.PENDIENTE));
        assertFalse(estado.puedeTransicionarA(EstadoPedido.CANCELADO));
    }
}
