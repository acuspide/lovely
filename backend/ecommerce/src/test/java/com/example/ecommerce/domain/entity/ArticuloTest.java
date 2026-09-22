package com.example.ecommerce.domain.entity;

import com.example.ecommerce.domain.exception.ReglaDominioException;
import com.example.ecommerce.domain.valueobject.CategoriaArticulo;
import com.example.ecommerce.domain.valueobject.NombreArticulo;
import com.example.ecommerce.domain.valueobject.Precio;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

public class ArticuloTest {

    private Articulo crearArticulo(long id, int stock) {
        return new Articulo(
                id,
                new NombreArticulo("Labial Mate"),
                "Labial de larga duración.",
                new Precio(new BigDecimal("20000")),
                CategoriaArticulo.MAQUILLAJE,
                "Maybelline",
                "https://ejemplo.com/labial.jpg",
                stock);
    }

    @Test
    void dosArticulosConElMismoIdDebenSerIguales() {
        // Arrange
        Articulo articulo1 = crearArticulo(1L, 10);
        Articulo articulo2 = new Articulo(
                1L, new NombreArticulo("Base"), "Otra descripción",
                new Precio(new BigDecimal("50000")), CategoriaArticulo.FACIAL,
                "MAC", "https://ejemplo.com/base.jpg", 20);

        // Assert
        assertEquals(articulo1, articulo2);
    }

    @Test
    void unArticuloNuevoConStockPositivoNoEstaAgotado() {
        assertFalse(crearArticulo(1L, 10).estaAgotado());
    }

    @Test
    void unArticuloConStockCeroEstaAgotado() {
        assertTrue(crearArticulo(1L, 0).estaAgotado());
    }

    @Test
    void actualizarCambiaLosCamposYRevalidaInvariantes() {
        // Arrange
        Articulo articulo = crearArticulo(1L, 10);

        // Act
        articulo.actualizar(
                new NombreArticulo("Labial Mate Rojo"), "Nueva descripción",
                new Precio(new BigDecimal("22000")), CategoriaArticulo.MAQUILLAJE,
                "Maybelline", "https://ejemplo.com/labial-rojo.jpg", 5);

        // Assert
        assertEquals("Labial Mate Rojo", articulo.getNombre().getValor());
        assertEquals(5, articulo.getStock());
    }

    @Test
    void noDebeCrearArticuloSinDescripcion() {
        assertThrows(ReglaDominioException.class, () -> new Articulo(
                1L, new NombreArticulo("Labial"), " ",
                new Precio(new BigDecimal("20000")), CategoriaArticulo.MAQUILLAJE,
                null, "https://ejemplo.com/labial.jpg", 10));
    }

    @Test
    void noDebeCrearArticuloSinCategoria() {
        assertThrows(ReglaDominioException.class, () -> new Articulo(
                1L, new NombreArticulo("Labial"), "Descripción",
                new Precio(new BigDecimal("20000")), null,
                null, "https://ejemplo.com/labial.jpg", 10));
    }

    @Test
    void noDebeCrearArticuloSinImagen() {
        assertThrows(ReglaDominioException.class, () -> new Articulo(
                1L, new NombreArticulo("Labial"), "Descripción",
                new Precio(new BigDecimal("20000")), CategoriaArticulo.MAQUILLAJE,
                null, " ", 10));
    }

    @Test
    void noDebeCrearArticuloConStockNegativo() {
        assertThrows(ReglaDominioException.class, () -> crearArticulo(1L, -1));
    }

    @Test
    void marcaEsOpcional() {
        Articulo articulo = new Articulo(
                1L, new NombreArticulo("Labial"), "Descripción",
                new Precio(new BigDecimal("20000")), CategoriaArticulo.MAQUILLAJE,
                null, "https://ejemplo.com/labial.jpg", 10);

        assertNull(articulo.getMarca());
    }
}
