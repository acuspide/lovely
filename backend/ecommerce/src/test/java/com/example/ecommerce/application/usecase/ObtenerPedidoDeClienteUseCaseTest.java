package com.example.ecommerce.application.usecase;

import com.example.ecommerce.domain.entity.Pedido;
import com.example.ecommerce.domain.exception.PedidoNoEncontradoException;
import com.example.ecommerce.domain.valueobject.DetallePedido;
import com.example.ecommerce.domain.valueobject.Precio;
import com.example.ecommerce.infrastructure.persistence.PedidoRepositoryEnMemoria;
import com.example.ecommerce.support.Pedidos;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/** RF-05.5: una clienta solo puede consultar sus propios pedidos. */
class ObtenerPedidoDeClienteUseCaseTest {

    @Test
    void laDuenaVeSuPedidoYOtraClientaNo() {
        // Arrange
        PedidoRepositoryEnMemoria pedidos = new PedidoRepositoryEnMemoria();
        Pedido pedido = pedidos.guardar(Pedidos.pendiente(7L,
                new DetallePedido(1L, "Labial", 1, new Precio(new BigDecimal("20000")))));
        ObtenerPedidoDeClienteUseCase useCase = new ObtenerPedidoDeClienteUseCase(pedidos);

        // Act y Assert
        assertEquals(pedido.getId(), useCase.ejecutar(pedido.getId(), 7L).getId());
        assertThrows(PedidoNoEncontradoException.class, () -> useCase.ejecutar(pedido.getId(), 8L));
        assertThrows(PedidoNoEncontradoException.class, () -> useCase.ejecutar(999L, 7L));
    }
}
