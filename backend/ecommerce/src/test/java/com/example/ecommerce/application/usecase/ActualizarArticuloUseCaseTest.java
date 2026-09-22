package com.example.ecommerce.application.usecase;

import com.example.ecommerce.domain.entity.Articulo;
import com.example.ecommerce.domain.exception.ArticuloNoEncontradoException;
import com.example.ecommerce.domain.exception.ReglaDominioException;
import com.example.ecommerce.domain.repository.ArticuloRepository;
import com.example.ecommerce.domain.valueobject.CategoriaArticulo;
import com.example.ecommerce.domain.valueobject.Precio;
import com.example.ecommerce.infrastructure.persistence.ArticuloRepositoryEnMemoria;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ActualizarArticuloUseCaseTest {

    @Test
    void debeActualizarUnArticuloExistente() {
        // Arrange
        ArticuloRepository repository = new ArticuloRepositoryEnMemoria();
        Articulo original = new CrearArticuloUseCase(repository).ejecutar(
                "Labial Mate", "Descripción", new Precio(new BigDecimal("20000")),
                CategoriaArticulo.MAQUILLAJE, null, "https://ejemplo.com/labial.jpg", 10);
        ActualizarArticuloUseCase useCase = new ActualizarArticuloUseCase(repository);

        // Act
        Articulo actualizado = useCase.ejecutar(
                original.getId(), "Labial Mate Rojo", "Nueva descripción",
                new Precio(new BigDecimal("22000")), CategoriaArticulo.MAQUILLAJE,
                "Maybelline", "https://ejemplo.com/labial-rojo.jpg", 3);

        // Assert
        assertEquals("Labial Mate Rojo", actualizado.getNombre().getValor());
        assertEquals(3, actualizado.getStock());
    }

    @Test
    void debeFallarSiElArticuloNoExiste() {
        // Arrange
        ArticuloRepository repository = new ArticuloRepositoryEnMemoria();
        ActualizarArticuloUseCase useCase = new ActualizarArticuloUseCase(repository);

        // Act y Assert
        assertThrows(ArticuloNoEncontradoException.class, () -> useCase.ejecutar(
                999L, "Labial", "Descripción", new Precio(new BigDecimal("20000")),
                CategoriaArticulo.MAQUILLAJE, null, "https://ejemplo.com/labial.jpg", 10));
    }

    @Test
    void noDebePermitirRenombrarAUnNombreYaUsadoPorOtroArticulo() {
        // Arrange
        ArticuloRepository repository = new ArticuloRepositoryEnMemoria();
        CrearArticuloUseCase crear = new CrearArticuloUseCase(repository);
        crear.ejecutar("Labial Mate", "Descripción", new Precio(new BigDecimal("20000")),
                CategoriaArticulo.MAQUILLAJE, null, "https://ejemplo.com/labial.jpg", 10);
        Articulo otro = crear.ejecutar("Base Líquida", "Descripción", new Precio(new BigDecimal("30000")),
                CategoriaArticulo.FACIAL, null, "https://ejemplo.com/base.jpg", 5);
        ActualizarArticuloUseCase useCase = new ActualizarArticuloUseCase(repository);

        // Act y Assert
        assertThrows(ReglaDominioException.class, () -> useCase.ejecutar(
                otro.getId(), "Labial Mate", "Descripción", new Precio(new BigDecimal("30000")),
                CategoriaArticulo.FACIAL, null, "https://ejemplo.com/base.jpg", 5));
    }

    @Test
    void debePermitirGuardarSinCambiarElPropioNombre() {
        // Arrange
        ArticuloRepository repository = new ArticuloRepositoryEnMemoria();
        Articulo original = new CrearArticuloUseCase(repository).ejecutar(
                "Labial Mate", "Descripción", new Precio(new BigDecimal("20000")),
                CategoriaArticulo.MAQUILLAJE, null, "https://ejemplo.com/labial.jpg", 10);
        ActualizarArticuloUseCase useCase = new ActualizarArticuloUseCase(repository);

        // Act
        Articulo actualizado = useCase.ejecutar(
                original.getId(), "Labial Mate", "Descripción actualizada",
                new Precio(new BigDecimal("21000")), CategoriaArticulo.MAQUILLAJE,
                null, "https://ejemplo.com/labial.jpg", 8);

        // Assert
        assertEquals("Descripción actualizada", actualizado.getDescripcion());
    }
}
