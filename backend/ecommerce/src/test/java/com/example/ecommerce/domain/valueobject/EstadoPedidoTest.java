package com.example.ecommerce.domain.valueobject;

import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
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

    @Test
    void elFlujoNormalSigueElDiagramaDeEstados() {
        // RF-07.3 / Figura 13
        assertEquals(Optional.of(EstadoPedido.CONFIRMADO), EstadoPedido.PENDIENTE.siguiente());
        assertEquals(Optional.of(EstadoPedido.EN_PROCESO), EstadoPedido.CONFIRMADO.siguiente());
        assertEquals(Optional.of(EstadoPedido.EN_CAMINO), EstadoPedido.EN_PROCESO.siguiente());
        assertEquals(Optional.of(EstadoPedido.ENTREGADO), EstadoPedido.EN_CAMINO.siguiente());
        assertEquals(Optional.of(EstadoPedido.COMPLETADO), EstadoPedido.ENTREGADO.siguiente());
        assertTrue(EstadoPedido.COMPLETADO.siguiente().isEmpty());
        assertTrue(EstadoPedido.CANCELADO.siguiente().isEmpty());
    }

    @Test
    void ningunEstadoVuelveAtras() {
        // RF-07.4
        assertFalse(EstadoPedido.ENTREGADO.puedeTransicionarA(EstadoPedido.EN_CAMINO));
        assertFalse(EstadoPedido.EN_CAMINO.puedeTransicionarA(EstadoPedido.EN_PROCESO));
        assertFalse(EstadoPedido.CONFIRMADO.puedeTransicionarA(EstadoPedido.PENDIENTE));
    }

    @Test
    void soloPendienteYConfirmadoAdmitenCancelacion() {
        // RF-07.5 / Figura 13
        for (EstadoPedido estado : EstadoPedido.values()) {
            boolean esperado = estado == EstadoPedido.PENDIENTE || estado == EstadoPedido.CONFIRMADO;
            assertEquals(esperado, estado.admiteCancelacion(), estado.name());
        }
    }

    @Test
    void completadoYCanceladoSonFinales() {
        // RF-07.4
        assertTrue(EstadoPedido.COMPLETADO.esFinal());
        assertTrue(EstadoPedido.CANCELADO.esFinal());
        assertFalse(EstadoPedido.ENTREGADO.esFinal());
    }
}
