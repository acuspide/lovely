package com.example.ecommerce.application.usecase;

import com.example.ecommerce.domain.entity.Articulo;
import com.example.ecommerce.domain.exception.NombreArticuloDuplicadoException;
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

    @Test
    void noDebePermitirElMismoNombreConOtrasMayusculasTildesOEspacios() {
        // Arrange
        ArticuloRepository repository = new ArticuloRepositoryEnMemoria();
        CrearArticuloUseCase useCase = new CrearArticuloUseCase(repository);
        useCase.ejecutar("Crema Hidratante", "Descripción", new Precio(new BigDecimal("35000")),
                CategoriaArticulo.FACIAL, null, "https://ejemplo.com/crema.jpg", 10);

        // Act y Assert (RF-02.3)
        NombreArticuloDuplicadoException error = assertThrows(NombreArticuloDuplicadoException.class,
                () -> useCase.ejecutar("  crema   HIDRATÁNTE ", "Otra", new Precio(new BigDecimal("30000")),
                        CategoriaArticulo.FACIAL, null, "https://ejemplo.com/crema2.jpg", 3));
        assertTrue(error.getMessage().contains("Crema Hidratante"));
    }

    @Test
    void debeGuardarElNombreSinEspaciosSobrantes() {
        // Arrange
        ArticuloRepository repository = new ArticuloRepositoryEnMemoria();
        CrearArticuloUseCase useCase = new CrearArticuloUseCase(repository);

        // Act
        Articulo articulo = useCase.ejecutar("  Base   Líquida ", "Descripción", new Precio(new BigDecimal("40000")),
                CategoriaArticulo.MAQUILLAJE, null, "https://ejemplo.com/base.jpg", 4);

        // Assert
        assertEquals("Base Líquida", articulo.getNombre().getValor());
    }
}
