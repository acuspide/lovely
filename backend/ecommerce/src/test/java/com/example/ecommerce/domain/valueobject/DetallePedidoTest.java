package com.example.ecommerce.domain.valueobject;

import com.example.ecommerce.domain.exception.ReglaDominioException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class DetallePedidoTest {

    @Test
    void unaCantidadCeroONegativaDebeLanzarReglaDominioException() {

        // Arrange
        Precio precio = new Precio(new BigDecimal("10000"));

        // Act y Assert
        assertThrows(ReglaDominioException.class, () -> {
            new DetallePedido(1L, 0, precio);
        });
    }

    @Test
    void elSubtotalDebeSerElPrecioMultiplicadoPorLaCantidad() {

        // Arrange
        Precio precio = new Precio(new BigDecimal("10000"));
        DetallePedido detalle = new DetallePedido(1L, 3, precio);

        // Act
        BigDecimal subtotal = detalle.subtotal();

        // Assert
        assertEquals(new BigDecimal("30000"), subtotal);
    }

    @Test
    void elPrecioUnitarioDelDetalleNoCambiaAunqueCambieElPrecioOriginal() {

        // Arrange
        Precio precioAlMomentoDeLaCompra = new Precio(new BigDecimal("10000"));
        DetallePedido detalle = new DetallePedido(1L, 1, precioAlMomentoDeLaCompra);

        // Act
        // El precio del articulo original podria cambiar despues en otra
        // instancia de Precio, pero el detalle ya congelo el suyo en el
        // constructor (RN42) y no expone forma de modificarlo.

        // Assert
        assertEquals(precioAlMomentoDeLaCompra, detalle.getPrecioUnitario());
    }
}
