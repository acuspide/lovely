package com.example.ecommerce.infrastructure.web.rest.dto;

import com.example.ecommerce.domain.entity.Pedido;
import com.example.ecommerce.domain.valueobject.QuienCancela;

import java.math.BigDecimal;
import java.time.Instant;

/** Patrón: DTO — una fila de "Mis pedidos" (RF-07.8). */
public record ResumenPedidoClienteResponse(
        long id,
        String numero,
        String estado,
        String tipoEntrega,
        Instant creadoEn,
        BigDecimal total,
        int unidades,
        boolean puedeCancelar) {

    public static ResumenPedidoClienteResponse desde(Pedido pedido) {
        return new ResumenPedidoClienteResponse(pedido.getId(), pedido.getNumero().valor(),
                pedido.getEstado().name(), pedido.getEntrega().getTipo().name(), pedido.getCreadoEn(),
                pedido.calcularTotal(), pedido.cantidadDeUnidades(),
                QuienCancela.CLIENTA.puedeCancelarDesde(pedido.getEstado()));
    }
}
