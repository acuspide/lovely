package com.example.ecommerce.support;

import com.example.ecommerce.domain.entity.Pedido;
import com.example.ecommerce.domain.valueobject.DatosEntrega;
import com.example.ecommerce.domain.valueobject.DetallePedido;
import com.example.ecommerce.domain.valueobject.TipoEntrega;

import java.time.Instant;
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

    /** Un pedido Pendiente sin guardar, para la clienta indicada. */
    public static Pedido pendiente(long clienteId, DetallePedido... detalles) {
        return Pedido.registrar(clienteId, List.of(detalles), retiroEnTienda(), AHORA, claveNueva());
    }
}
