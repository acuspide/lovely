package com.example.ecommerce.application.usecase;

import com.example.ecommerce.domain.entity.Articulo;
import com.example.ecommerce.domain.entity.Carrito;
import com.example.ecommerce.domain.exception.ArticuloAgotadoException;
import com.example.ecommerce.domain.exception.ArticuloInactivoException;
import com.example.ecommerce.domain.exception.ReglaDominioException;
import com.example.ecommerce.domain.repository.ArticuloRepository;
import com.example.ecommerce.domain.repository.CarritoRepository;
import com.example.ecommerce.domain.valueobject.CategoriaArticulo;
import com.example.ecommerce.domain.valueobject.NombreArticulo;
import com.example.ecommerce.domain.valueobject.Precio;
import com.example.ecommerce.infrastructure.persistence.ArticuloRepositoryEnMemoria;
import com.example.ecommerce.infrastructure.persistence.CarritoRepositoryEnMemoria;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class AgregarAlCarritoUseCaseTest {

    private Articulo crearArticuloPublicado(ArticuloRepository articuloRepository) {
        Articulo articulo = new Articulo(
                1L,
                new NombreArticulo("Labial"),
                "Labial de larga duración",
                new Precio(new BigDecimal("20000")),
                CategoriaArticulo.MAQUILLAJE,
                "Maybelline",
                "https://ejemplo.com/labial.jpg",
                10
        );
        return articuloRepository.guardar(articulo);
    }

    @Test
    void debeAgregarUnArticuloAUnCarritoNuevo() {

        // Arrange
        ArticuloRepository articuloRepository = new ArticuloRepositoryEnMemoria();
        CarritoRepository carritoRepository = new CarritoRepositoryEnMemoria();
        crearArticuloPublicado(articuloRepository);
        AgregarAlCarritoUseCase useCase = new AgregarAlCarritoUseCase(carritoRepository, articuloRepository);

        // Act
        Carrito carrito = useCase.ejecutar(1L, 100L, 1L, 2);

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
            useCase.ejecutar(1L, 100L, 99L, 1);
        });
    }

    @Test
    void noDebePermitirAgregarUnArticuloDesactivado() {

        // Arrange (RF-02.7)
        ArticuloRepository articuloRepository = new ArticuloRepositoryEnMemoria();
        CarritoRepository carritoRepository = new CarritoRepositoryEnMemoria();
        Articulo articulo = crearArticuloPublicado(articuloRepository);
        new CambiarEstadoArticuloUseCase(articuloRepository).ejecutar(articulo.getId(), false);
        AgregarAlCarritoUseCase useCase = new AgregarAlCarritoUseCase(carritoRepository, articuloRepository);

        // Act y Assert
        assertThrows(ArticuloInactivoException.class, () ->
                useCase.ejecutar(1L, 100L, articulo.getId(), 1));
    }

    @Test
    void noDebePermitirAgregarUnArticuloAgotado() {

        // Arrange (RF-03.4 / RN01): producto activo pero con stock 0
        ArticuloRepository articuloRepository = new ArticuloRepositoryEnMemoria();
        CarritoRepository carritoRepository = new CarritoRepositoryEnMemoria();
        Articulo agotado = articuloRepository.guardar(new Articulo(
                0L, new NombreArticulo("Base Agotada"), "Descripción",
                new Precio(new BigDecimal("30000")), CategoriaArticulo.FACIAL,
                null, null, 0));
        AgregarAlCarritoUseCase useCase = new AgregarAlCarritoUseCase(carritoRepository, articuloRepository);

        // Act y Assert
        assertThrows(ArticuloAgotadoException.class, () -> useCase.ejecutar(1L, 100L, agotado.getId(), 1));
    }

    @Test
    void usaElPrecioGuardadoDelProductoYNoUnoExterno() {

        // Arrange
        ArticuloRepository articuloRepository = new ArticuloRepositoryEnMemoria();
        CarritoRepository carritoRepository = new CarritoRepositoryEnMemoria();
        Articulo articulo = crearArticuloPublicado(articuloRepository);
        AgregarAlCarritoUseCase useCase = new AgregarAlCarritoUseCase(carritoRepository, articuloRepository);

        // Act
        Carrito carrito = useCase.ejecutar(1L, 100L, articulo.getId(), 3);

        // Assert: 3 x 20000
        assertEquals(new BigDecimal("60000"), carrito.calcularTotal());
    }
}
