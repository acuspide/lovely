package com.example.ecommerce.application.usecase;

import com.example.ecommerce.domain.entity.*;
import com.example.ecommerce.domain.repository.ArticuloRepository;
import com.example.ecommerce.domain.valueobject.*;
import com.example.ecommerce.infrastructure.persistence.ArticuloRepositoryEnMemoria;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class PublicarArticuloUseCaseTest {

    @Test
    void debePublicarArticuloCorrectamente() {

        // Arrange
        ArticuloRepository repository = new ArticuloRepositoryEnMemoria();

        PublicarArticuloUseCase useCase =
                new PublicarArticuloUseCase(repository);

        // Act
        Articulo articulo = useCase.ejecutar(
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

        // Assert
        assertTrue(articulo.isPublicado());
    }
}
