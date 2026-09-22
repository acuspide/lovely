package com.example.ecommerce.domain.entity;

import com.example.ecommerce.domain.exception.ReglaDominioException;
import com.example.ecommerce.domain.valueobject.Precio;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class CarritoTest {

    @Test
    void unCarritoNuevoDebeEstarVacio() {

        // Arrange
        Carrito carrito = new Carrito(1L, 100L);

        // Act
        // No necesitamos una accion adicional, el carrito recien
        // creado ya debe estar vacio.

        // Assert
        assertTrue(carrito.estaVacio());
    }

    @Test
    void agregarUnItemDebeSumarloAlTotal() {

        // Arrange
        Carrito carrito = new Carrito(1L, 100L);
        Precio precio = new Precio(new BigDecimal("10000"));

        // Act
        carrito.agregarItem(1L, 2, precio, 5);

        // Assert
        assertEquals(new BigDecimal("20000"), carrito.calcularTotal());
    }

    @Test
    void agregarElMismoArticuloDosVecesDebeAcumularLaCantidad() {

        // Arrange
        Carrito carrito = new Carrito(1L, 100L);
        Precio precio = new Precio(new BigDecimal("10000"));

        // Act
        carrito.agregarItem(1L, 2, precio, 5);
        carrito.agregarItem(1L, 2, precio, 5);

        // Assert
        assertEquals(new BigDecimal("40000"), carrito.calcularTotal());
    }

    @Test
    void agregarUnaCantidadMayorAlStockDebeLanzarReglaDominioException() {

        // Arrange
        Carrito carrito = new Carrito(1L, 100L);
        Precio precio = new Precio(new BigDecimal("10000"));

        // Act y Assert
        assertThrows(ReglaDominioException.class, () -> {
            carrito.agregarItem(1L, 10, precio, 3);
        });
    }

    @Test
    void eliminarUnArticuloQueNoEstaEnElCarritoDebeLanzarReglaDominioException() {

        // Arrange
        Carrito carrito = new Carrito(1L, 100L);

        // Act y Assert
        assertThrows(ReglaDominioException.class, () -> {
            carrito.eliminarItem(99L);
        });
    }

    @Test
    void vaciarElCarritoDebeDejarloSinItems() {

        // Arrange
        Carrito carrito = new Carrito(1L, 100L);
        Precio precio = new Precio(new BigDecimal("10000"));
        carrito.agregarItem(1L, 2, precio, 5);

        // Act
        carrito.vaciar();

        // Assert
        assertTrue(carrito.estaVacio());
    }
}
