package com.example.ecommerce.domain.entity;

import com.example.ecommerce.domain.exception.CancelacionNoPermitidaException;
import com.example.ecommerce.domain.exception.ClaveIdempotenciaInvalidaException;
import com.example.ecommerce.domain.exception.MotivoCancelacionInvalidoException;
import com.example.ecommerce.domain.exception.PagoInvalidoException;
import com.example.ecommerce.domain.exception.PagoNoConfirmadoException;
import com.example.ecommerce.domain.exception.PedidoSinDetallesException;
import com.example.ecommerce.domain.exception.ReglaDominioException;
import com.example.ecommerce.domain.exception.TransicionEstadoInvalidaException;
import com.example.ecommerce.domain.valueobject.CambioEstadoPedido;
import com.example.ecommerce.domain.valueobject.DetallePedido;
import com.example.ecommerce.domain.valueobject.EstadoPedido;
import com.example.ecommerce.domain.valueobject.MedioPago;
import com.example.ecommerce.domain.valueobject.PagoConfirmado;
import com.example.ecommerce.domain.valueobject.Precio;
import com.example.ecommerce.domain.valueobject.QuienCancela;
import com.example.ecommerce.support.Pedidos;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** F-05 y F-07: reglas del pedido y de sus cambios de estado. */
public class PedidoTest {

    private static final Precio DIEZ_MIL = new Precio(new BigDecimal("10000"));
    private static final long ASESORA = 7L;
    private static final long ADMIN = 1L;

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
        // RF-07.4
        Pedido pedido = Pedidos.pendiente(100L, new DetallePedido(1L, "Labial", 2, DIEZ_MIL));
        pedido.cancelar(QuienCancela.CLIENTA, 100L, "Me equivoqué de tono", Pedidos.AHORA);

        assertThrows(ReglaDominioException.class,
                () -> pedido.avanzarA(EstadoPedido.CONFIRMADO, ASESORA, Pedidos.AHORA));
        assertFalse(pedido.estaActivo());
    }

    @Test
    void elRegistroEsLaPrimeraEntradaDelHistorial() {
        // RF-07.7
        Pedido pedido = Pedidos.pendiente(100L, new DetallePedido(1L, "Labial", 1, DIEZ_MIL));

        assertEquals(1, pedido.getHistorial().size());
        CambioEstadoPedido registro = pedido.getHistorial().get(0);
        assertNull(registro.estadoAnterior());
        assertEquals(EstadoPedido.PENDIENTE, registro.estadoNuevo());
        assertEquals(100L, registro.usuarioId());
    }

    @Test
    void recorreTodoElFlujoNormalYCadaPasoQuedaEnElHistorial() {
        // RF-07.3 / RF-07.7
        Pedido pedido = Pedidos.pendiente(100L, new DetallePedido(1L, "Labial", 1, DIEZ_MIL));
        Instant despues = Pedidos.AHORA.plusSeconds(60);

        pedido.confirmarPago(Pedidos.pagoPorElTotal(pedido, ASESORA, despues));
        for (EstadoPedido siguiente : List.of(EstadoPedido.EN_PROCESO,
                EstadoPedido.EN_CAMINO, EstadoPedido.ENTREGADO, EstadoPedido.COMPLETADO)) {
            pedido.avanzarA(siguiente, ASESORA, despues);
        }

        assertEquals(EstadoPedido.COMPLETADO, pedido.getEstado());
        assertEquals(6, pedido.getHistorial().size());
        CambioEstadoPedido ultimo = pedido.getHistorial().get(5);
        assertEquals(EstadoPedido.ENTREGADO, ultimo.estadoAnterior());
        assertEquals(EstadoPedido.COMPLETADO, ultimo.estadoNuevo());
        assertEquals(ASESORA, ultimo.usuarioId());
        assertEquals(despues, ultimo.fecha());
    }

    @Test
    void noSePuedeSaltarUnEstado() {
        // RF-07.3: de Pendiente solo se pasa a Confirmado
        Pedido pedido = Pedidos.pendiente(100L, new DetallePedido(1L, "Labial", 1, DIEZ_MIL));

        assertThrows(TransicionEstadoInvalidaException.class,
                () -> pedido.avanzarA(EstadoPedido.EN_CAMINO, ASESORA, Pedidos.AHORA));
        assertEquals(EstadoPedido.PENDIENTE, pedido.getEstado());
        assertEquals(1, pedido.getHistorial().size(), "un intento rechazado no deja rastro");
    }

    @Test
    void unPedidoEntregadoNoVuelveAtras() {
        // RF-07.4
        Pedido pedido = avanzadoHasta(EstadoPedido.ENTREGADO);

        assertThrows(TransicionEstadoInvalidaException.class,
                () -> pedido.avanzarA(EstadoPedido.EN_CAMINO, ASESORA, Pedidos.AHORA));
        assertThrows(TransicionEstadoInvalidaException.class,
                () -> pedido.cancelar(QuienCancela.ADMINISTRADORA, ADMIN, "Tarde", Pedidos.AHORA));
    }

    @Test
    void laCancelacionExigeMotivo() {
        // RF-07.5
        Pedido pedido = Pedidos.pendiente(100L, new DetallePedido(1L, "Labial", 1, DIEZ_MIL));

        assertThrows(MotivoCancelacionInvalidoException.class,
                () -> pedido.cancelar(QuienCancela.CLIENTA, 100L, "   ", Pedidos.AHORA));
        assertThrows(MotivoCancelacionInvalidoException.class,
                () -> pedido.cancelar(QuienCancela.CLIENTA, 100L, "x".repeat(301), Pedidos.AHORA));
        assertEquals(EstadoPedido.PENDIENTE, pedido.getEstado());
    }

    @Test
    void laCancelacionGuardaElMotivoYQuienCancelo() {
        // RF-07.5 / RF-07.7
        Pedido pedido = avanzadoHasta(EstadoPedido.CONFIRMADO);

        pedido.cancelar(QuienCancela.ADMINISTRADORA, ADMIN, "  Sin stock en bodega  ", Pedidos.AHORA);

        assertEquals(EstadoPedido.CANCELADO, pedido.getEstado());
        assertEquals("Sin stock en bodega", pedido.getMotivoCancelacion().orElseThrow());
        assertEquals(ADMIN, pedido.getHistorial().get(pedido.getHistorial().size() - 1).usuarioId());
    }

    @Test
    void laClientaNoPuedeCancelarUnPedidoConfirmado() {
        // RF-07.5: desde Confirmado solo cancela la Administradora
        Pedido pedido = avanzadoHasta(EstadoPedido.CONFIRMADO);

        assertThrows(CancelacionNoPermitidaException.class,
                () -> pedido.cancelar(QuienCancela.CLIENTA, 100L, "Ya no lo quiero", Pedidos.AHORA));
        assertEquals(EstadoPedido.CONFIRMADO, pedido.getEstado());
    }

    @Test
    void enProcesoYaNoSePuedeCancelar() {
        // RF-07.5 / Figura 13
        Pedido pedido = avanzadoHasta(EstadoPedido.EN_PROCESO);

        assertThrows(TransicionEstadoInvalidaException.class,
                () -> pedido.cancelar(QuienCancela.ADMINISTRADORA, ADMIN, "Motivo", Pedidos.AHORA));
    }

    private static Pedido avanzadoHasta(EstadoPedido destino) {
        Pedido pedido = Pedidos.pendiente(100L, new DetallePedido(1L, "Labial", 1, DIEZ_MIL));
        while (pedido.getEstado() != destino) {
            if (pedido.getEstado() == EstadoPedido.PENDIENTE) {
                Pedidos.confirmarPago(pedido, ASESORA);
            } else {
                pedido.avanzarA(pedido.getEstado().siguiente().orElseThrow(), ASESORA, Pedidos.AHORA);
            }
        }
        return pedido;
    }

    @Test
    void elTotalDelPedidoDebeSerLaSumaDeLosSubtotalesDeSusDetalles() {
        Pedido pedido = Pedidos.pendiente(100L,
                new DetallePedido(1L, "Labial", 2, DIEZ_MIL), new DetallePedido(2L, "Base", 1, DIEZ_MIL));

        assertEquals(new BigDecimal("30000"), pedido.calcularTotal());
        assertEquals(3, pedido.cantidadDeUnidades());
        assertTrue(pedido.incluyeArticulo(2L));
    }

    // ---- F-09: confirmación del pago (RN11) ----

    @Test
    void unPedidoPendienteNoPasaAConfirmadoSinRegistrarElPago() {
        // RN11 / RF-09.6
        Pedido pedido = Pedidos.pendiente(100L, new DetallePedido(1L, "Labial", 2, DIEZ_MIL));

        assertThrows(PagoNoConfirmadoException.class,
                () -> pedido.avanzarA(EstadoPedido.CONFIRMADO, ASESORA, Pedidos.AHORA));
        assertEquals(EstadoPedido.PENDIENTE, pedido.getEstado());
        assertTrue(pedido.getPago().isEmpty());
    }

    @Test
    void alConfirmarElPagoQuedanSusDatosYElPedidoPasaAConfirmado() {
        // RF-09.2 / RF-09.5 / RF-07.7
        Pedido pedido = Pedidos.pendiente(100L, new DetallePedido(1L, "Labial", 2, DIEZ_MIL));
        Instant despues = Pedidos.AHORA.plusSeconds(1800);

        pedido.confirmarPago(new PagoConfirmado(MedioPago.TRANSFERENCIA, "  TRX-998877  ", new BigDecimal("20000"),
                LocalDate.of(2026, 10, 16), ASESORA, despues));

        assertEquals(EstadoPedido.CONFIRMADO, pedido.getEstado());
        PagoConfirmado pago = pedido.getPago().orElseThrow();
        assertEquals("TRX-998877", pago.referencia());
        assertEquals(ASESORA, pago.confirmadoPor());
        CambioEstadoPedido ultimo = pedido.getHistorial().get(1);
        assertEquals(EstadoPedido.PENDIENTE, ultimo.estadoAnterior());
        assertEquals(EstadoPedido.CONFIRMADO, ultimo.estadoNuevo());
        assertEquals(ASESORA, ultimo.usuarioId());
        assertEquals(despues, ultimo.fecha());
    }

    @Test
    void elMontoRecibidoDebeSerIgualAlTotal() {
        // RF-09.3: 2 × $10.000 = $20.000
        Pedido pedido = Pedidos.pendiente(100L, new DetallePedido(1L, "Labial", 2, DIEZ_MIL));

        var error = assertThrows(PagoInvalidoException.class, () -> pedido.confirmarPago(new PagoConfirmado(
                MedioPago.EFECTIVO, null, new BigDecimal("15000"), LocalDate.of(2026, 10, 16), ASESORA,
                Pedidos.AHORA)));
        assertTrue(error.getMessage().contains("no coincide con el total"));
        assertEquals(EstadoPedido.PENDIENTE, pedido.getEstado());
    }

    @Test
    void unaTransferenciaExigeReferenciaYElEfectivoNo() {
        // RF-09.2
        assertThrows(PagoInvalidoException.class, () -> new PagoConfirmado(MedioPago.TRANSFERENCIA, "  ",
                new BigDecimal("20000"), LocalDate.of(2026, 10, 16), ASESORA, Pedidos.AHORA));
        PagoConfirmado efectivo = new PagoConfirmado(MedioPago.EFECTIVO, null, new BigDecimal("20000"),
                LocalDate.of(2026, 10, 16), ASESORA, Pedidos.AHORA);
        assertNull(efectivo.referencia());
    }

    @Test
    void laFechaDeRecepcionNoPuedeSerFuturaNiAnteriorAlPedido() {
        // RF-09.4: el pedido es del 16 de octubre y se confirma ese mismo día
        Pedido pedido = Pedidos.pendiente(100L, new DetallePedido(1L, "Labial", 2, DIEZ_MIL));
        BigDecimal total = pedido.calcularTotal();

        assertThrows(PagoInvalidoException.class, () -> pedido.confirmarPago(new PagoConfirmado(
                MedioPago.EFECTIVO, null, total, LocalDate.of(2026, 10, 17), ASESORA, Pedidos.AHORA)));
        assertThrows(PagoInvalidoException.class, () -> pedido.confirmarPago(new PagoConfirmado(
                MedioPago.EFECTIVO, null, total, LocalDate.of(2026, 10, 15), ASESORA, Pedidos.AHORA)));
        assertEquals(EstadoPedido.PENDIENTE, pedido.getEstado());
    }

    @Test
    void elPagoSoloSeConfirmaUnaVezYNoEnUnPedidoCancelado() {
        // RF-09.1 / RF-07.4
        Pedido confirmado = Pedidos.pendiente(100L, new DetallePedido(1L, "Labial", 1, DIEZ_MIL));
        Pedidos.confirmarPago(confirmado, ASESORA);
        Pedido cancelado = Pedidos.pendiente(100L, new DetallePedido(1L, "Labial", 1, DIEZ_MIL));
        cancelado.cancelar(QuienCancela.CLIENTA, 100L, "Ya no lo necesito", Pedidos.AHORA);

        assertThrows(TransicionEstadoInvalidaException.class, () -> Pedidos.confirmarPago(confirmado, ASESORA));
        assertThrows(TransicionEstadoInvalidaException.class, () -> Pedidos.confirmarPago(cancelado, ASESORA));
    }
}
