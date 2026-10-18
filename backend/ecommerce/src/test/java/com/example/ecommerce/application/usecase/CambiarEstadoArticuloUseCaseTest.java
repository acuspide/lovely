package com.example.ecommerce.application.usecase;

import com.example.ecommerce.domain.entity.Articulo;
import com.example.ecommerce.domain.exception.ArticuloNoEncontradoException;
import com.example.ecommerce.domain.repository.ArticuloRepository;
import com.example.ecommerce.domain.valueobject.CategoriaArticulo;
import com.example.ecommerce.domain.valueobject.Precio;
import com.example.ecommerce.infrastructure.persistence.ArticuloRepositoryEnMemoria;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** RF-02.7: desactivar y reactivar productos. */
class CambiarEstadoArticuloUseCaseTest {

    private Articulo crearArticulo(ArticuloRepository repository) {
        return new CrearArticuloUseCase(repository).ejecutar(
                "Labial Mate", "Descripción", new Precio(new BigDecimal("20000")),
                CategoriaArticulo.MAQUILLAJE, null, "https://ejemplo.com/labial.jpg", 10);
    }

    @Test
    void unProductoNuevoNaceActivo() {
        // Arrange
        ArticuloRepository repository = new ArticuloRepositoryEnMemoria();

        // Act
        Articulo articulo = crearArticulo(repository);

        // Assert
        assertTrue(articulo.isActivo());
    }

    @Test
    void desactivarOcultaElProductoDelCatalogoPeroNoLoBorra() {
        // Arrange
        ArticuloRepository repository = new ArticuloRepositoryEnMemoria();
        Articulo articulo = crearArticulo(repository);
        CambiarEstadoArticuloUseCase useCase = new CambiarEstadoArticuloUseCase(repository);
        ListarArticulosUseCase listar = new ListarArticulosUseCase(repository);

        // Act
        Articulo desactivado = useCase.ejecutar(articulo.getId(), false);

        // Assert
        assertFalse(desactivado.isActivo());
        assertTrue(listar.ejecutar(Optional.empty(), Optional.empty()).isEmpty(), "no sale en el catálogo");
        assertEquals(1, listar.ejecutarParaGestion(Optional.empty(), Optional.empty()).size(), "sí sale en el panel");
    }

    @Test
    void reactivarLoVuelveAMostrarEnElCatalogo() {
        // Arrange
        ArticuloRepository repository = new ArticuloRepositoryEnMemoria();
        Articulo articulo = crearArticulo(repository);
        CambiarEstadoArticuloUseCase useCase = new CambiarEstadoArticuloUseCase(repository);
        useCase.ejecutar(articulo.getId(), false);

        // Act
        Articulo reactivado = useCase.ejecutar(articulo.getId(), true);

        // Assert
        assertTrue(reactivado.isActivo());
        assertEquals(1, new ListarArticulosUseCase(repository).ejecutar(Optional.empty(), Optional.empty()).size());
    }

    @Test
    void elDetallePublicoDeUnProductoInactivoNoSeEncuentra() {
        // Arrange
        ArticuloRepository repository = new ArticuloRepositoryEnMemoria();
        Articulo articulo = crearArticulo(repository);
        new CambiarEstadoArticuloUseCase(repository).ejecutar(articulo.getId(), false);
        ObtenerArticuloUseCase obtener = new ObtenerArticuloUseCase(repository);

        // Act y Assert
        assertThrows(ArticuloNoEncontradoException.class, () -> obtener.ejecutar(articulo.getId()));
        assertFalse(obtener.ejecutarParaGestion(articulo.getId()).isActivo());
    }

    @Test
    void debeFallarSiElProductoNoExiste() {
        // Arrange
        CambiarEstadoArticuloUseCase useCase = new CambiarEstadoArticuloUseCase(new ArticuloRepositoryEnMemoria());

        // Act y Assert
        assertThrows(ArticuloNoEncontradoException.class, () -> useCase.ejecutar(999L, false));
    }
}
