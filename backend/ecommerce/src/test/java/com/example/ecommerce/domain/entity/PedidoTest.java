package com.example.ecommerce.domain.entity;

import com.example.ecommerce.domain.exception.ClaveIdempotenciaInvalidaException;
import com.example.ecommerce.domain.exception.PedidoSinDetallesException;
import com.example.ecommerce.domain.exception.ReglaDominioException;
import com.example.ecommerce.domain.valueobject.DetallePedido;
import com.example.ecommerce.domain.valueobject.EstadoPedido;
import com.example.ecommerce.domain.valueobject.Precio;
import com.example.ecommerce.support.Pedidos;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** F-05: reglas del pedido. */
public class PedidoTest {

    private static final Precio DIEZ_MIL = new Precio(new BigDecimal("10000"));

    @Test
    void unPedidoNuevoNaceEnEstadoPendiente() {
        // RF-05.1
        Pedido pedido = Pedidos.pendiente(100L, new DetallePedido(1L, "Labial", 2, DIEZ_MIL));

        assertEquals(EstadoPedido.PENDIENTE, pedido.getEstado());
        assertEquals(Pedidos.AHORA, pedido.getCreadoEn());
    }

    @Test
    void unPedidoSinProductosNoSePuedeCrear() {
        // RN16 / RF-05.3
        assertThrows(PedidoSinDetallesException.class, () -> Pedido.registrar(
                100L, List.of(), Pedidos.retiroEnTienda(), Pedidos.AHORA, Pedidos.claveNueva()));
    }

    @Test
    void exigeUnaClaveDeIntentoValida() {
        // RF-05.7
        assertThrows(ClaveIdempotenciaInvalidaException.class, () -> Pedido.registrar(
                100L, List.of(new DetallePedido(1L, "Labial", 1, DIEZ_MIL)), Pedidos.retiroEnTienda(),
                Pedidos.AHORA, "no-es-un-uuid"));
    }

    @Test
    void elNumeroDePedidoSaleDelConsecutivo() {
        // RF-05.2
        Pedido guardado = Pedidos.pendiente(100L, new DetallePedido(1L, "Labial", 1, DIEZ_MIL)).conId(42L);

        assertEquals("LG-000042", guardado.getNumero().valor());
    }

    @Test
    void unPedidoCanceladoNuncaDebeVolverAConfirmarse() {
        Pedido pedido = Pedidos.pendiente(100L, new DetallePedido(1L, "Labial", 2, DIEZ_MIL));
        pedido.cancelar();

        assertThrows(ReglaDominioException.class, pedido::confirmar);
        assertFalse(pedido.estaActivo());
    }

    @Test
    void elTotalDelPedidoDebeSerLaSumaDeLosSubtotalesDeSusDetalles() {
        Pedido pedido = Pedidos.pendiente(100L,
                new DetallePedido(1L, "Labial", 2, DIEZ_MIL), new DetallePedido(2L, "Base", 1, DIEZ_MIL));

        assertEquals(new BigDecimal("30000"), pedido.calcularTotal());
        assertEquals(3, pedido.cantidadDeUnidades());
        assertTrue(pedido.incluyeArticulo(2L));
    }
}
