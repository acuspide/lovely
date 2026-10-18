package com.example.ecommerce.domain.entity;

import com.example.ecommerce.domain.valueobject.DetallePedido;
import com.example.ecommerce.domain.valueobject.EstadoFactura;
import com.example.ecommerce.domain.valueobject.Precio;
import com.example.ecommerce.support.Pedidos;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** F-08: reglas de la factura. */
public class FacturaTest {

    private static final BigDecimal IVA = new BigDecimal("0.19");

    @Test
    void unaFacturaNuevaQuedaEmitida() {
        // RF-08.1
        Factura factura = emitir(5L);

        assertEquals(EstadoFactura.EMITIDA, factura.getEstado());
        assertFalse(factura.estaAnulada());
    }

    @Test
    void noHayFacturaSinPedido() {
        // RN02
        assertThrows(IllegalArgumentException.class, () -> emitir(0L));
    }

    @Test
    void alAnularseConservaSuNumeroYLaFecha() {
        // RF-08.6
        Factura factura = emitir(5L).conId(3L);
        var despues = Pedidos.AHORA.plusSeconds(3600);

        factura.anular(despues);
        factura.anular(despues.plusSeconds(60));

        assertTrue(factura.estaAnulada());
        assertEquals("FV-000003", factura.getNumero().valor());
        assertEquals(despues, factura.getAnuladaEn().orElseThrow(), "anular dos veces no cambia la fecha");
    }

    @Test
    void elDesgloseUsaElTotalDelPedido() {
        // RF-08.3
        Pedido pedido = Pedidos.pendiente(100L,
                new DetallePedido(1L, "Labial", 2, new Precio(new BigDecimal("59500")))).conId(5L);

        var desglose = emitir(5L).desglosar(pedido);

        assertEquals(0, new BigDecimal("119000").compareTo(desglose.total()));
        assertEquals(0, new BigDecimal("19000").compareTo(desglose.iva()));
    }

    @Test
    void noSeDesglosaConElPedidoEquivocado() {
        Pedido otro = Pedidos.pendiente(100L, new DetallePedido(1L, "Labial", 1, new Precio(BigDecimal.TEN))).conId(9L);

        assertThrows(IllegalArgumentException.class, () -> emitir(5L).desglosar(otro));
    }

    private static Factura emitir(long pedidoId) {
        return Factura.emitir(pedidoId, "Camila Ríos", "camila@correo.com", "3001234567", IVA, Pedidos.AHORA);
    }
}
