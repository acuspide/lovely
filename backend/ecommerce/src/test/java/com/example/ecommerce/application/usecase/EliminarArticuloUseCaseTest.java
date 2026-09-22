package com.example.ecommerce.application.usecase;

import com.example.ecommerce.domain.entity.Articulo;
import com.example.ecommerce.domain.entity.Pedido;
import com.example.ecommerce.domain.exception.ArticuloConPedidosActivosException;
import com.example.ecommerce.domain.exception.ArticuloNoEncontradoException;
import com.example.ecommerce.domain.repository.ArticuloRepository;
import com.example.ecommerce.domain.repository.PedidoRepository;
import com.example.ecommerce.domain.valueobject.CategoriaArticulo;
import com.example.ecommerce.domain.valueobject.DetallePedido;
import com.example.ecommerce.domain.valueobject.Precio;
import com.example.ecommerce.infrastructure.persistence.ArticuloRepositoryEnMemoria;
import com.example.ecommerce.infrastructure.persistence.PedidoRepositoryEnMemoria;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EliminarArticuloUseCaseTest {

    private Articulo crearArticulo(ArticuloRepository repository) {
        return new CrearArticuloUseCase(repository).ejecutar(
                "Labial Mate", "Descripción", new Precio(new BigDecimal("20000")),
                CategoriaArticulo.MAQUILLAJE, null, "https://ejemplo.com/labial.jpg", 10);
    }

    @Test
    void debeEliminarUnArticuloSinPedidosActivos() {
        // Arrange
        ArticuloRepository articuloRepository = new ArticuloRepositoryEnMemoria();
        PedidoRepository pedidoRepository = new PedidoRepositoryEnMemoria();
        Articulo articulo = crearArticulo(articuloRepository);
        EliminarArticuloUseCase useCase = new EliminarArticuloUseCase(articuloRepository, pedidoRepository);

        // Act
        useCase.ejecutar(articulo.getId());

        // Assert
        assertTrue(articuloRepository.obtenerPorId(articulo.getId()).isEmpty());
    }

    @Test
    void debeFallarSiElArticuloNoExiste() {
        // Arrange
        ArticuloRepository articuloRepository = new ArticuloRepositoryEnMemoria();
        PedidoRepository pedidoRepository = new PedidoRepositoryEnMemoria();
        EliminarArticuloUseCase useCase = new EliminarArticuloUseCase(articuloRepository, pedidoRepository);

        // Act y Assert
        assertThrows(ArticuloNoEncontradoException.class, () -> useCase.ejecutar(999L));
    }

    @Test
    void rn17_noDebeEliminarUnArticuloConUnPedidoActivo() {
        // Arrange
        ArticuloRepository articuloRepository = new ArticuloRepositoryEnMemoria();
        PedidoRepository pedidoRepository = new PedidoRepositoryEnMemoria();
        Articulo articulo = crearArticulo(articuloRepository);

        Pedido pedido = new Pedido(1L, 100L);
        pedido.agregarDetalle(new DetallePedido(articulo.getId(), 1, articulo.getPrecio()));
        pedido.confirmar();
        pedidoRepository.guardar(pedido);

        EliminarArticuloUseCase useCase = new EliminarArticuloUseCase(articuloRepository, pedidoRepository);

        // Act y Assert
        assertThrows(ArticuloConPedidosActivosException.class, () -> useCase.ejecutar(articulo.getId()));
        assertTrue(articuloRepository.obtenerPorId(articulo.getId()).isPresent());
    }

    @Test
    void siElUnicoPedidoEstaCanceladoSiPuedeEliminarse() {
        // Arrange
        ArticuloRepository articuloRepository = new ArticuloRepositoryEnMemoria();
        PedidoRepository pedidoRepository = new PedidoRepositoryEnMemoria();
        Articulo articulo = crearArticulo(articuloRepository);

        Pedido pedido = new Pedido(1L, 100L);
        pedido.agregarDetalle(new DetallePedido(articulo.getId(), 1, articulo.getPrecio()));
        pedido.cancelar();
        pedidoRepository.guardar(pedido);

        EliminarArticuloUseCase useCase = new EliminarArticuloUseCase(articuloRepository, pedidoRepository);

        // Act
        useCase.ejecutar(articulo.getId());

        // Assert
        assertTrue(articuloRepository.obtenerPorId(articulo.getId()).isEmpty());
    }
}
