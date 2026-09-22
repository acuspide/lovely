package com.example.ecommerce.application.usecase;

import com.example.ecommerce.domain.entity.*;
import com.example.ecommerce.domain.exception.ReglaDominioException;
import com.example.ecommerce.domain.repository.ArticuloRepository;
import com.example.ecommerce.domain.repository.CarritoRepository;
import com.example.ecommerce.domain.valueobject.*;
import com.example.ecommerce.infrastructure.persistence.ArticuloRepositoryEnMemoria;
import com.example.ecommerce.infrastructure.persistence.CarritoRepositoryEnMemoria;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class AgregarAlCarritoUseCaseTest {

    private Articulo crearArticuloPublicado(ArticuloRepository articuloRepository) {
        Articulo articulo = new Articulo(
                1L,
                new NombreArticulo("Labial"),
                new Precio(new BigDecimal("20000")),
                new Categoria(1L, "Labios"),
                new Marca(1L, "Maybelline"),
                Tono.OSCURO,
                List.of(TipoPiel.NORMAL),
                new Inventario(10),
                new FechaVencimiento(LocalDate.of(2027, 12, 31)),
                new Tienda(1L, "Tienda Beauty")
        );
        articulo.publicar();
        articuloRepository.guardar(articulo);
        return articulo;
    }

    @Test
    void debeAgregarUnArticuloAUnCarritoNuevo() {

        // Arrange
        ArticuloRepository articuloRepository = new ArticuloRepositoryEnMemoria();
        CarritoRepository carritoRepository = new CarritoRepositoryEnMemoria();
        crearArticuloPublicado(articuloRepository);
        AgregarAlCarritoUseCase useCase = new AgregarAlCarritoUseCase(carritoRepository, articuloRepository);

        // Act
        Carrito carrito = useCase.ejecutar(1L, 100L, 1L, 2, new Precio(new BigDecimal("20000")), 10);

        // Assert
        assertEquals(new BigDecimal("40000"), carrito.calcularTotal());
    }

    @Test
    void noDebePermitirAgregarUnArticuloQueNoExisteEnElRepositorio() {

        // Arrange
        ArticuloRepository articuloRepository = new ArticuloRepositoryEnMemoria();
        CarritoRepository carritoRepository = new CarritoRepositoryEnMemoria();
        AgregarAlCarritoUseCase useCase = new AgregarAlCarritoUseCase(carritoRepository, articuloRepository);

        // Act y Assert
        assertThrows(ReglaDominioException.class, () -> {
            useCase.ejecutar(1L, 100L, 99L, 1, new Precio(new BigDecimal("20000")), 5);
        });
    }
}
