package com.example.ecommerce.application.usecase;

import com.example.ecommerce.domain.entity.Articulo;
import com.example.ecommerce.domain.exception.ReglaDominioException;
import com.example.ecommerce.domain.repository.ArticuloRepository;
import com.example.ecommerce.domain.valueobject.CategoriaArticulo;
import com.example.ecommerce.domain.valueobject.Precio;
import com.example.ecommerce.infrastructure.persistence.ArticuloRepositoryEnMemoria;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CrearArticuloUseCaseTest {

    @Test
    void debeCrearUnArticuloNuevoConNombreUnico() {
        // Arrange
        ArticuloRepository repository = new ArticuloRepositoryEnMemoria();
        CrearArticuloUseCase useCase = new CrearArticuloUseCase(repository);

        // Act
        Articulo articulo = useCase.ejecutar(
                "Labial Mate", "Larga duración", new Precio(new BigDecimal("20000")),
                CategoriaArticulo.MAQUILLAJE, "Maybelline", "https://ejemplo.com/labial.jpg", 10);

        // Assert
        assertTrue(articulo.getId() > 0);
        assertEquals(CategoriaArticulo.MAQUILLAJE, articulo.getCategoria());
    }

    @Test
    void noDebePermitirDosArticulosConElMismoNombre() {
        // Arrange
        ArticuloRepository repository = new ArticuloRepositoryEnMemoria();
        CrearArticuloUseCase useCase = new CrearArticuloUseCase(repository);
        useCase.ejecutar("Labial Mate", "Descripción", new Precio(new BigDecimal("20000")),
                CategoriaArticulo.MAQUILLAJE, null, "https://ejemplo.com/labial.jpg", 10);

        // Act y Assert
        assertThrows(ReglaDominioException.class, () -> useCase.ejecutar(
                "Labial Mate", "Otra descripción", new Precio(new BigDecimal("18000")),
                CategoriaArticulo.MAQUILLAJE, null, "https://ejemplo.com/labial2.jpg", 5));
    }
}
