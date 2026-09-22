package com.example.ecommerce.domain.valueobject;

import com.example.ecommerce.domain.exception.ReglaDominioException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class ItemCarritoTest {

    @Test
    void unaCantidadCeroONegativaDebeLanzarReglaDominioException() {

        // Arrange
        Precio precio = new Precio(new BigDecimal("10000"));

        // Act y Assert
        assertThrows(ReglaDominioException.class, () -> {
            new ItemCarrito(1L, 0, precio, 5);
        });
    }

    @Test
    void unaCantidadMayorAlStockDisponibleDebeLanzarReglaDominioException() {

        // Arrange
        Precio precio = new Precio(new BigDecimal("10000"));

        // Act y Assert
        assertThrows(ReglaDominioException.class, () -> {
            new ItemCarrito(1L, 10, precio, 3);
        });
    }

    @Test
    void elSubtotalDebeSerElPrecioMultiplicadoPorLaCantidad() {

        // Arrange
        Precio precio = new Precio(new BigDecimal("10000"));
        ItemCarrito item = new ItemCarrito(1L, 3, precio, 5);

        // Act
        BigDecimal subtotal = item.subtotal();

        // Assert
        assertEquals(new BigDecimal("30000"), subtotal);
    }

    @Test
    void dosItemsConElMismoArticuloIdDebenSerIguales() {

        // Arrange
        Precio precio = new Precio(new BigDecimal("10000"));
        ItemCarrito item1 = new ItemCarrito(1L, 2, precio, 5);
        ItemCarrito item2 = new ItemCarrito(1L, 4, precio, 5);

        // Act
        // No necesitamos una accion adicional porque equals()
        // es la operacion que queremos probar.

        // Assert
        assertEquals(item1, item2);
    }
}
