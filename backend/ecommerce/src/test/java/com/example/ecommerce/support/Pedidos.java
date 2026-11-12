package com.example.ecommerce.support;

import com.example.ecommerce.domain.entity.Pedido;
import com.example.ecommerce.domain.valueobject.DatosEntrega;
import com.example.ecommerce.domain.valueobject.DetallePedido;
import com.example.ecommerce.domain.valueobject.MedioPago;
import com.example.ecommerce.domain.valueobject.PagoConfirmado;
import com.example.ecommerce.domain.valueobject.TipoEntrega;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;

/** Datos de prueba para pedidos (F-05). */
public final class Pedidos {

    public static final Instant AHORA = Instant.parse("2026-10-16T15:00:00Z");

    private Pedidos() {
    }

    public static DatosEntrega retiroEnTienda() {
        return new DatosEntrega(TipoEntrega.RETIRO_EN_TIENDA, "Ana Pérez", "3001234567", null, null, null);
    }

    public static String claveNueva() {
        return UUID.randomUUID().toString();
    }

    /**
     * F-09: confirma el pago del pedido con una transferencia por el total exacto,
     * recibida el día del pedido. Es la única forma de pasar de Pendiente a Confirmado.
     */
    public static void confirmarPago(Pedido pedido, long usuarioId) {
        pedido.confirmarPago(pagoPorElTotal(pedido, usuarioId, AHORA));
    }

    /** F-09: datos válidos de un pago por transferencia por el total del pedido. */
    public static PagoConfirmado pagoPorElTotal(Pedido pedido, long usuarioId, Instant confirmadoEn) {
        return new PagoConfirmado(MedioPago.TRANSFERENCIA, "TRX-0001", pedido.calcularTotal(),
                LocalDate.ofInstant(pedido.getCreadoEn(), ZoneId.of("America/Bogota")), usuarioId, confirmadoEn);
    }

    /** Un pedido Pendiente sin guardar, para la clienta indicada. */
    public static Pedido pendiente(long clienteId, DetallePedido... detalles) {
        return Pedido.registrar(clienteId, List.of(detalles), retiroEnTienda(), AHORA, claveNueva());
    }
}
