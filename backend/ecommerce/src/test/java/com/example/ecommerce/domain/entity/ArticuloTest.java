package com.example.ecommerce.domain.entity;

import com.example.ecommerce.domain.exception.ReglaDominioException;
import com.example.ecommerce.domain.valueobject.*;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class ArticuloTest {
    @Test
    void dosArticulosConElMismoIdDebenSerIguales() {

        // Arrange
        Articulo articulo1 = new Articulo(
                1L,
                new NombreArticulo("Labial"),
                new Precio(new BigDecimal("20000")),
                new Categoria(1L, "Labios"),
                new Marca(1L, "Maybelline"),
                Tono.CLARO,
                List.of(TipoPiel.NORMAL),
                new Inventario(10),
                new FechaVencimiento(LocalDate.of(2027, 12, 31)),
                new Tienda(1L, "Tienda Beauty")
        );

        Articulo articulo2 = new Articulo(
                1L,
                new NombreArticulo("Base"),
                new Precio(new BigDecimal("50000")),
                new Categoria(2L, "Rostro"),
                new Marca(2L, "MAC"),
                Tono.OSCURO,
                List.of(TipoPiel.SECA),
                new Inventario(20),
                new FechaVencimiento(LocalDate.of(2028, 5, 20)),
                new Tienda(2L, "Otra Tienda")
        );

        // Act
        // La comparación se realiza directamente en el Assert.

        // Assert
        assertEquals(articulo1, articulo2);
    }
    @Test
    void noDebePermitirPublicarArticuloVencido() {

        // Arrange
        Articulo articulo = new Articulo(
                2L,
                new NombreArticulo("Labial"),
                new Precio(new BigDecimal("20000")),
                new Categoria(1L, "Labios"),
                new Marca(1L, "Maybelline"),
                Tono.OSCURO,
                List.of(TipoPiel.NORMAL),
                new Inventario(10),
                new FechaVencimiento(LocalDate.of(2025, 1, 1)),
                new Tienda(1L, "Tienda Beauty")
        );

        // Act y Assert
        assertThrows(ReglaDominioException.class, () -> {
            articulo.publicar();
        });

        // Verificamos que el estado no cambió
        assertFalse(articulo.isPublicado());
    }
    @Test
    void noDebePermitirPublicarArticuloSinCategoria() {

        // Arrange
        Articulo articulo = new Articulo(
                3L,
                new NombreArticulo("Labial"),
                new Precio(new BigDecimal("20000")),
                null,
                new Marca(1L, "Maybelline"),
                Tono.OSCURO,
                List.of(TipoPiel.NORMAL),
                new Inventario(10),
                new FechaVencimiento(LocalDate.of(2027, 12, 31)),
                new Tienda(1L, "Tienda Beauty")
        );

        // Act y Assert
        assertThrows(ReglaDominioException.class, () -> {
            articulo.publicar();
        });

        // El estado no debe haber cambiado
        assertFalse(articulo.isPublicado());
    }
    @Test
    void noDebePermitirPublicarArticuloSinPrecio() {

        // Arrange
        Articulo articulo = new Articulo(
                4L,
                new NombreArticulo("Labial"),
                null,
                new Categoria(1L, "Labios"),
                new Marca(1L, "Maybelline"),
                Tono.OSCURO,
                List.of(TipoPiel.NORMAL),
                new Inventario(10),
                new FechaVencimiento(LocalDate.of(2027, 12, 31)),
                new Tienda(1L, "Tienda Beauty")
        );

        // Act y Assert
        assertThrows(ReglaDominioException.class, () -> {
            articulo.publicar();
        });

        // El estado no debe haber cambiado
        assertFalse(articulo.isPublicado());
    }
}
