package com.example.ecommerce.application.usecase;

import com.example.ecommerce.application.usecase.RevisarCarritoUseCase.LineaSolicitada;
import com.example.ecommerce.domain.entity.Articulo;
import com.example.ecommerce.domain.exception.CantidadInvalidaException;
import com.example.ecommerce.domain.exception.CarritoDemasiadoGrandeException;
import com.example.ecommerce.domain.repository.ArticuloRepository;
import com.example.ecommerce.domain.valueobject.CategoriaArticulo;
import com.example.ecommerce.domain.valueobject.EstadoLineaCarrito;
import com.example.ecommerce.domain.valueobject.LineaCarritoRevisada;
import com.example.ecommerce.domain.valueobject.Precio;
import com.example.ecommerce.domain.valueobject.RevisionCarrito;
import com.example.ecommerce.infrastructure.persistence.ArticuloRepositoryEnMemoria;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** F-04: revisión del carrito contra el inventario (RF-04.1, RF-04.2, RF-04.7, CO-01). */
class RevisarCarritoUseCaseTest {

    private ArticuloRepository repository;
    private RevisarCarritoUseCase useCase;
    private Articulo labial;   // 20.000, stock 10
    private Articulo base;     // 30.000, stock 2

    @BeforeEach
    void preparar() {
        repository = new ArticuloRepositoryEnMemoria();
        CrearArticuloUseCase crear = new CrearArticuloUseCase(repository);
        labial = crear.ejecutar("Labial Mate", "Descripción", new Precio(new BigDecimal("20000")),
                CategoriaArticulo.MAQUILLAJE, null, null, 10);
        base = crear.ejecutar("Base Líquida", "Descripción", new Precio(new BigDecimal("30000")),
                CategoriaArticulo.FACIAL, null, null, 2);
        useCase = new RevisarCarritoUseCase(repository);
    }

    @Test
    void conStockSuficienteCalculaElTotalConLosPreciosGuardados() {
        // Act
        RevisionCarrito revision = useCase.ejecutar(List.of(
                new LineaSolicitada(labial.getId(), 2), new LineaSolicitada(base.getId(), 1)));

        // Assert: 2 × 20.000 + 1 × 30.000
        assertEquals(0, new BigDecimal("70000").compareTo(revision.total()));
        assertEquals(3, revision.cantidadDeUnidades());
        assertFalse(revision.requiereAtencion());
    }

    @Test
    void siPideMasDeLoQueHayAjustaLaCantidadAlStock() {
        // Act (RF-04.2 / CU-02 A2)
        LineaCarritoRevisada linea = useCase.ejecutar(List.of(new LineaSolicitada(base.getId(), 5)))
                .getLineas().get(0);

        // Assert
        assertEquals(EstadoLineaCarrito.AJUSTADA, linea.getEstado());
        assertEquals(2, linea.getCantidad());
        assertEquals(5, linea.getCantidadSolicitada());
    }

    @Test
    void unProductoAgotadoQuedaConCantidadCeroYNoSumaAlTotal() {
        // Arrange (RF-04.7 / CU-02 A3: se agotó entre la selección y la confirmación)
        Articulo aceite = new CrearArticuloUseCase(repository).ejecutar("Aceite Capilar", "Descripción",
                new Precio(new BigDecimal("15000")), CategoriaArticulo.CAPILAR, null, null, 0);

        // Act
        RevisionCarrito revision = useCase.ejecutar(List.of(
                new LineaSolicitada(labial.getId(), 1), new LineaSolicitada(aceite.getId(), 1)));

        // Assert
        assertEquals(EstadoLineaCarrito.AGOTADO, revision.getLineas().get(1).getEstado());
        assertEquals(0, new BigDecimal("20000").compareTo(revision.total()));
        assertTrue(revision.requiereAtencion());
        assertTrue(revision.tieneProductosComprables());
    }

    @Test
    void unProductoDesactivadoOInexistenteNoEstaDisponible() {
        // Arrange (RF-02.7)
        new CambiarEstadoArticuloUseCase(repository).ejecutar(labial.getId(), false);

        // Act
        RevisionCarrito revision = useCase.ejecutar(List.of(
                new LineaSolicitada(labial.getId(), 1), new LineaSolicitada(999L, 1)));

        // Assert
        assertEquals(EstadoLineaCarrito.NO_DISPONIBLE, revision.getLineas().get(0).getEstado());
        assertEquals(EstadoLineaCarrito.NO_DISPONIBLE, revision.getLineas().get(1).getEstado());
        assertFalse(revision.tieneProductosComprables());
    }

    @Test
    void siUnProductoVieneRepetidoSumaLasCantidades() {
        // Act
        RevisionCarrito revision = useCase.ejecutar(List.of(
                new LineaSolicitada(labial.getId(), 1), new LineaSolicitada(labial.getId(), 2)));

        // Assert
        assertEquals(1, revision.getLineas().size());
        assertEquals(3, revision.getLineas().get(0).getCantidad());
    }

    @Test
    void laCantidadDebeSerMayorQueCero() {
        // RN19
        assertThrows(CantidadInvalidaException.class,
                () -> useCase.ejecutar(List.of(new LineaSolicitada(labial.getId(), 0))));
    }

    @Test
    void revisarNoModificaElInventario() {
        // CO-01: operación de solo lectura
        useCase.ejecutar(List.of(new LineaSolicitada(base.getId(), 2)));

        assertEquals(2, repository.obtenerPorId(base.getId()).orElseThrow().getStock());
    }

    @Test
    void rechazaCarritosConDemasiadosProductosDistintos() {
        List<LineaSolicitada> muchas = IntStream.rangeClosed(1, RevisarCarritoUseCase.MAXIMO_PRODUCTOS_DISTINTOS + 1)
                .mapToObj(id -> new LineaSolicitada(id, 1))
                .toList();

        assertThrows(CarritoDemasiadoGrandeException.class, () -> useCase.ejecutar(muchas));
    }
}
