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

    @Test
    void buscaSinImportarTildesNiEspacios() {
        // Arrange (RF-02.5): "Base Líquida" debe aparecer buscando "liquida".
        ListarArticulosUseCase useCase = new ListarArticulosUseCase(repositoryConDatos());

        // Act
        List<Articulo> resultado = useCase.ejecutar(Optional.empty(), Optional.of("  liquida "));

        // Assert
        assertEquals(1, resultado.size());
        assertEquals("Base Líquida", resultado.get(0).getNombre().getValor());
    }

    @Test
    void unaBusquedaEnBlancoNoFiltra() {
        // Arrange
        ListarArticulosUseCase useCase = new ListarArticulosUseCase(repositoryConDatos());

        // Act
        List<Articulo> resultado = useCase.ejecutar(Optional.empty(), Optional.of("   "));

        // Assert
        assertEquals(3, resultado.size());
    }

    @Test
    void devuelveLosProductosEnOrdenAlfabetico() {
        // Arrange
        ListarArticulosUseCase useCase = new ListarArticulosUseCase(repositoryConDatos());

        // Act
        List<String> nombres = useCase.ejecutarParaGestion(Optional.empty(), Optional.empty()).stream()
                .map(articulo -> articulo.getNombre().getValor())
                .toList();

        // Assert
        assertEquals(List.of("Base Líquida", "Labial Mate", "Shampoo Reparador"), nombres);
    }

    @Test
    void combinaCategoriaYBusqueda() {
        // Arrange
        ListarArticulosUseCase useCase = new ListarArticulosUseCase(repositoryConDatos());

        // Act: "a" está en los tres nombres, pero solo uno es CAPILAR
        List<Articulo> resultado = useCase.ejecutar(Optional.of(CategoriaArticulo.CAPILAR), Optional.of("a"));

        // Assert
        assertEquals(1, resultado.size());
        assertEquals("Shampoo Reparador", resultado.get(0).getNombre().getValor());
    }

    @Test
    void ordenaPorPrecioDeMenorAMayorYDeMayorAMenor() {
        // Arrange (RF-03.6): Labial 20000, Shampoo 25000, Base 30000
        ListarArticulosUseCase useCase = new ListarArticulosUseCase(repositoryConDatos());

        // Act
        List<String> ascendente = nombres(useCase.ejecutar(Optional.empty(), Optional.empty(), OrdenCatalogo.PRECIO_MENOR));
        List<String> descendente = nombres(useCase.ejecutar(Optional.empty(), Optional.empty(), OrdenCatalogo.PRECIO_MAYOR));

        // Assert
        assertEquals(List.of("Labial Mate", "Shampoo Reparador", "Base Líquida"), ascendente);
        assertEquals(List.of("Base Líquida", "Shampoo Reparador", "Labial Mate"), descendente);
    }

    @Test
    void enElCatalogoLosAgotadosVanAlFinalSinImportarElOrden() {
        // Arrange (RF-03.4): un producto barato pero agotado
        ArticuloRepository repository = repositoryConDatos();
        new CrearArticuloUseCase(repository).ejecutar("Aceite Capilar", "Descripción",
                new Precio(new BigDecimal("5000")), CategoriaArticulo.CAPILAR, null, null, 0);
        ListarArticulosUseCase useCase = new ListarArticulosUseCase(repository);

        // Act: aunque es el más barato, no debe salir primero
        List<String> resultado = nombres(useCase.ejecutar(Optional.empty(), Optional.empty(), OrdenCatalogo.PRECIO_MENOR));

        // Assert
        assertEquals("Aceite Capilar", resultado.get(resultado.size() - 1));
        assertEquals("Labial Mate", resultado.get(0));
    }

    private static List<String> nombres(List<Articulo> articulos) {
        return articulos.stream().map(articulo -> articulo.getNombre().getValor()).toList();
    }
}
