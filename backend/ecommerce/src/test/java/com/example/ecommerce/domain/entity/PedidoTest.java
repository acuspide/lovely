package com.example.ecommerce.domain.entity;

import com.example.ecommerce.domain.exception.ReglaDominioException;
import com.example.ecommerce.domain.valueobject.DetallePedido;
import com.example.ecommerce.domain.valueobject.EstadoPedido;
import com.example.ecommerce.domain.valueobject.Precio;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class PedidoTest {

    @Test
    void unPedidoSinDetallesNoDebePoderConfirmarse() {

        // Arrange
        Pedido pedido = new Pedido(1L, 100L);

        // Act y Assert
        assertThrows(ReglaDominioException.class, pedido::confirmar);
    }

    @Test
    void unPedidoConAlMenosUnDetalleDebePoderConfirmarse() {

        // Arrange
        Pedido pedido = new Pedido(1L, 100L);
        Precio precio = new Precio(new BigDecimal("10000"));
        pedido.agregarDetalle(new DetallePedido(1L, 2, precio));

        // Act
        pedido.confirmar();

        // Assert
        assertEquals(EstadoPedido.CONFIRMADO, pedido.getEstado());
    }

    @Test
    void unPedidoCanceladoNuncaDebeVolverAConfirmarse() {

        // Arrange
        Pedido pedido = new Pedido(1L, 100L);
        Precio precio = new Precio(new BigDecimal("10000"));
        pedido.agregarDetalle(new DetallePedido(1L, 2, precio));
        pedido.cancelar();

        // Act y Assert
        assertThrows(ReglaDominioException.class, pedido::confirmar);
    }

    @Test
    void elTotalDelPedidoDebeSerLaSumaDeLosSubtotalesDeSusDetalles() {

        // Arrange
        Pedido pedido = new Pedido(1L, 100L);
        Precio precio = new Precio(new BigDecimal("10000"));
        pedido.agregarDetalle(new DetallePedido(1L, 2, precio));
        pedido.agregarDetalle(new DetallePedido(2L, 1, precio));

        // Act
        BigDecimal total = pedido.calcularTotal();

        // Assert
        assertEquals(new BigDecimal("30000"), total);
    }

    @Test
    void elPrecioDeUnDetalleYaAgregadoNoCambiaSiCambiaElPrecioDelArticulo() {

        // Arrange
        Pedido pedido = new Pedido(1L, 100L);
        Precio precioAlMomentoDeLaCompra = new Precio(new BigDecimal("10000"));
        pedido.agregarDetalle(new DetallePedido(1L, 1, precioAlMomentoDeLaCompra));

        // Act
        // Simulamos que el precio del articulo cambia despues de la compra
        // creando otra instancia de Precio; el detalle ya congelo el suyo
        // y el total del pedido no debe verse afectado.
        new Precio(new BigDecimal("15000"));

        // Assert
        assertEquals(new BigDecimal("10000"), pedido.calcularTotal());
    }
}
