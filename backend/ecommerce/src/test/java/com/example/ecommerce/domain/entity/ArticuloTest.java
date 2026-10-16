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
                "Maybelline", "https://ejemplo.com/labial-rojo.jpg");

        // Assert
        assertEquals("Labial Mate Rojo", articulo.getNombre().getValor());
        assertEquals(10, articulo.getStock(), "RF-02.6: editar no cambia el stock");
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
    void laImagenEsOpcionalYUnTextoVacioSeGuardaComoSinImagen() {
        // RF-02.4 / CU-01 A4: se puede continuar sin imagen.
        Articulo articulo = new Articulo(
                1L, new NombreArticulo("Labial"), "Descripción",
                new Precio(new BigDecimal("20000")), CategoriaArticulo.MAQUILLAJE,
                null, " ", 10);

        assertNull(articulo.getImagenUrl());
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

    @Test
    void unProductoConStockSePuedeVender() {
        // RF-03.4
        assertDoesNotThrow(() -> crearArticulo(1L, 5).verificarDisponibleParaVenta());
    }

    @Test
    void unProductoAgotadoNoSePuedeVender() {
        // RF-03.4 / RN01 / RN12
        assertThrows(com.example.ecommerce.domain.exception.ArticuloAgotadoException.class,
                () -> crearArticulo(1L, 0).verificarDisponibleParaVenta());
    }

    @Test
    void unProductoInactivoNoSePuedeVenderAunqueTengaStock() {
        // RF-02.7
        Articulo articulo = crearArticulo(1L, 5);
        articulo.desactivar();

        assertThrows(com.example.ecommerce.domain.exception.ArticuloInactivoException.class,
                articulo::verificarDisponibleParaVenta);
    }

    @Test
    void descontarStockRestaLasUnidadesVendidas() {
        // RF-06.1
        Articulo articulo = crearArticulo(1L, 10);

        articulo.descontarStock(3);

        assertEquals(7, articulo.getStock());
    }

    @Test
    void siNoAlcanzaNoDescuentaNadaEInformaCuantasQuedan() {
        // RF-06.3 / RF-06.4 / RN20
        Articulo articulo = crearArticulo(1L, 2);

        var error = assertThrows(com.example.ecommerce.domain.exception.StockInsuficienteException.class,
                () -> articulo.descontarStock(3));

        assertEquals(2, articulo.getStock());
        assertTrue(error.getMessage().contains("Solo quedan 2 unidades"));
    }

    @Test
    void alVenderLaUltimaUnidadQuedaAgotado() {
        // RF-06.6 / RN12
        Articulo articulo = crearArticulo(1L, 1);

        articulo.descontarStock(1);

        assertEquals(0, articulo.getStock());
        assertTrue(articulo.estaAgotado());
    }

    @Test
    void laCantidadADescontarDebeSerPositiva() {
        // RN19
        assertThrows(com.example.ecommerce.domain.exception.CantidadInvalidaException.class,
                () -> crearArticulo(1L, 5).descontarStock(0));
    }
}
