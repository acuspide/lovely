package com.example.ecommerce.application.usecase;

import com.example.ecommerce.domain.entity.Articulo;
import com.example.ecommerce.domain.repository.ArticuloRepository;
import com.example.ecommerce.domain.valueobject.CategoriaArticulo;
import com.example.ecommerce.domain.valueobject.Precio;
import com.example.ecommerce.infrastructure.persistence.ArticuloRepositoryEnMemoria;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ListarArticulosUseCaseTest {

    private ArticuloRepository repositoryConDatos() {
        ArticuloRepository repository = new ArticuloRepositoryEnMemoria();
        CrearArticuloUseCase crear = new CrearArticuloUseCase(repository);
        crear.ejecutar("Labial Mate", "Descripción", new Precio(new BigDecimal("20000")),
                CategoriaArticulo.MAQUILLAJE, null, "https://ejemplo.com/labial.jpg", 10);
        crear.ejecutar("Base Líquida", "Descripción", new Precio(new BigDecimal("30000")),
                CategoriaArticulo.FACIAL, null, "https://ejemplo.com/base.jpg", 5);
        crear.ejecutar("Shampoo Reparador", "Descripción", new Precio(new BigDecimal("25000")),
                CategoriaArticulo.CAPILAR, null, "https://ejemplo.com/shampoo.jpg", 8);
        return repository;
    }

    @Test
    void sinFiltrosDevuelveTodosLosArticulos() {
        // Arrange
        ListarArticulosUseCase useCase = new ListarArticulosUseCase(repositoryConDatos());

        // Act
        List<Articulo> resultado = useCase.ejecutar(Optional.empty(), Optional.empty());

        // Assert
        assertEquals(3, resultado.size());
    }

    @Test
    void filtraPorCategoria() {
        // Arrange
        ListarArticulosUseCase useCase = new ListarArticulosUseCase(repositoryConDatos());

        // Act
        List<Articulo> resultado = useCase.ejecutar(Optional.of(CategoriaArticulo.FACIAL), Optional.empty());

        // Assert
        assertEquals(1, resultado.size());
        assertEquals("Base Líquida", resultado.get(0).getNombre().getValor());
    }

    @Test
    void buscaPorNombreSinImportarMayusculas() {
        // Arrange
        ListarArticulosUseCase useCase = new ListarArticulosUseCase(repositoryConDatos());

        // Act
        List<Articulo> resultado = useCase.ejecutar(Optional.empty(), Optional.of("LABIAL"));

        // Assert
        assertEquals(1, resultado.size());
        assertEquals("Labial Mate", resultado.get(0).getNombre().getValor());
    }
}
